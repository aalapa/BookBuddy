package com.bookbuddy.repository

import com.bookbuddy.data.*
import kotlinx.coroutines.flow.Flow
import java.util.*

class BookRepository(
    private val bookDao: BookDao,
    private val categoryDao: CategoryDao
) {
    fun getBooksToRead(): Flow<List<Book>> = bookDao.getBooksToRead()
    fun getCompletedBooks(): Flow<List<Book>> = bookDao.getCompletedBooks()
    fun getInProgressBooks(): Flow<List<Book>> = bookDao.getInProgressBooks()
    fun getTotalBooksRead(): Flow<Int> = bookDao.getTotalBooksRead()
    fun getBooksInQueueCount(): Flow<Int> = bookDao.getBooksInQueueCount()
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    fun getAllAuthors(): Flow<List<String>> = bookDao.getAllAuthors()
    fun getAllCategoriesForFilter(): Flow<List<String>> = bookDao.getAllCategoriesForFilter()

    suspend fun getBookById(id: Long): Book? = bookDao.getBookById(id)
    suspend fun insertBook(book: Book): Long = bookDao.insertBook(book)
    suspend fun updateBook(book: Book) = bookDao.updateBook(book)
    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book)
        // Re-rank remaining books to read
        reRankBooksToRead()
    }

    suspend fun markAsInProgress(bookId: Long) {
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            val now = Date()
            // If first time starting (no startDate), set it. Otherwise preserve original startDate
            val startDate = book.startDate ?: now
            bookDao.updateBookStatus(bookId, BookStatus.IN_PROGRESS, startDate)
            // Set current reading start date (new session starts now)
            // Preserve totalReadingDays (accumulated from previous sessions if any)
            bookDao.updateReadingStatus(bookId, BookStatus.IN_PROGRESS, now, book.totalReadingDays)
        }
    }

    suspend fun markAsOnHold(bookId: Long) {
        val book = bookDao.getBookById(bookId)
        if (book != null && book.status == BookStatus.IN_PROGRESS) {
            val now = Date()
            // Calculate days from current reading session start to now
            val daysToAdd = book.currentReadingStartDate?.let { startDate ->
                calculateDaysBetween(startDate, now)
            } ?: 0
            // Add to total reading days (accumulate)
            val newTotalDays = book.totalReadingDays + daysToAdd
            // Clear current reading start date (paused), keep totalReadingDays
            bookDao.updateReadingStatus(bookId, BookStatus.ON_HOLD, null, newTotalDays)
        }
    }

    suspend fun markAsCompleted(bookId: Long) {
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            val now = Date()
            // If currently reading, add current session days to total
            val daysToAdd = if (book.status == BookStatus.IN_PROGRESS && book.currentReadingStartDate != null) {
                calculateDaysBetween(book.currentReadingStartDate!!, now)
            } else {
                0
            }
            val finalTotalDays = book.totalReadingDays + daysToAdd
            // Update total reading days and clear current session before marking as completed
            bookDao.updateReadingStatus(bookId, BookStatus.COMPLETED, null, finalTotalDays)
            bookDao.markAsCompleted(bookId, BookStatus.COMPLETED, now)
            // Re-rank remaining books to read
            reRankBooksToRead()
        }
    }

    private fun calculateDaysBetween(start: Date, end: Date): Int {
        val calendar = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply { time = start }
        val endCal = Calendar.getInstance().apply { time = end }
        
        // Reset to midnight for accurate day calculation
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        
        endCal.set(Calendar.HOUR_OF_DAY, 0)
        endCal.set(Calendar.MINUTE, 0)
        endCal.set(Calendar.SECOND, 0)
        endCal.set(Calendar.MILLISECOND, 0)
        
        val diffInMillis = endCal.timeInMillis - startCal.timeInMillis
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
    }

    suspend fun getBooksToReadFiltered(author: String?, category: String?, titleSearch: String?, sortBy: String): List<Book> {
        val books = bookDao.getBooksToReadFiltered(
            if (author.isNullOrBlank()) null else author,
            if (category.isNullOrBlank()) null else category,
            if (titleSearch.isNullOrBlank()) null else titleSearch
        )
        // Sort the results
        return when (sortBy) {
            "date_asc" -> books.sortedBy { it.createdAt }
            "date_desc" -> books.sortedByDescending { it.createdAt }
            else -> books.sortedBy { it.ranking } // Default: by ranking
        }
    }

    suspend fun getAllBooks(): List<Book> = bookDao.getAllBooks()

    suspend fun getBooksReadThisYear(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, Calendar.JANUARY)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYear = Date(calendar.timeInMillis)
        val allCompleted = bookDao.getAllCompletedBooks()
        return allCompleted.count { book ->
            book.endDate != null && book.endDate!! >= startOfYear
        }
    }

    suspend fun calculateRunRate(): Double {
        val completedBooks = bookDao.getAllCompletedBooks()
        if (completedBooks.isEmpty()) return 0.0

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        // Check if current year is a leap year
        val isLeapYear = (currentYear % 4 == 0 && currentYear % 100 != 0) || (currentYear % 400 == 0)
        val totalDaysInYear = if (isLeapYear) 366 else 365

        val booksThisYear = completedBooks.count { book ->
            book.endDate?.let { endDate ->
                val bookCalendar = Calendar.getInstance().apply { time = endDate }
                bookCalendar.get(Calendar.YEAR) == currentYear
            } ?: false
        }

        return if (currentDayOfYear > 0) {
            (booksThisYear.toDouble() / currentDayOfYear) * totalDaysInYear
        } else {
            0.0
        }
    }

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    suspend fun getCategoryByName(name: String): Category? = categoryDao.getCategoryByName(name)

    suspend fun updateBookRankings(books: List<Book>) {
        // Update all rankings in a transaction
        books.forEachIndexed { index, book ->
            bookDao.updateRanking(book.id, index + 1)
        }
    }

    suspend fun reorderBook(fromPosition: Int, toPosition: Int, books: List<Book>) {
        if (fromPosition == toPosition || fromPosition < 0 || toPosition < 0 || 
            fromPosition >= books.size || toPosition >= books.size) {
            return
        }

        // Create a mutable copy of the list
        val reorderedBooks = books.toMutableList()
        val movedBook = reorderedBooks.removeAt(fromPosition)
        reorderedBooks.add(toPosition, movedBook)

        // Update all rankings based on new positions
        reorderedBooks.forEachIndexed { index, book ->
            val newRanking = index + 1
            if (book.ranking != newRanking) {
                bookDao.updateRanking(book.id, newRanking)
            }
        }
    }

    /**
     * Re-ranks all books to read (NOT_STARTED, IN_PROGRESS, ON_HOLD) sequentially starting from 1
     * This is called after a book is deleted or marked as completed
     */
    private suspend fun reRankBooksToRead() {
        // Get all books that should be in the "to read" list
        // This includes NOT_STARTED, IN_PROGRESS, and ON_HOLD (but not COMPLETED)
        val allBooks = bookDao.getAllBooks()
        val booksToRead = allBooks
            .filter { it.status != BookStatus.COMPLETED }
            .sortedBy { it.ranking } // Sort by current ranking to maintain order
        
        // Re-assign rankings sequentially starting from 1
        booksToRead.forEachIndexed { index, book ->
            val newRanking = index + 1
            if (book.ranking != newRanking) {
                bookDao.updateRanking(book.id, newRanking)
            }
        }
    }
}

