package com.bookbuddy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE status != 'COMPLETED' ORDER BY ranking ASC")
    fun getBooksToRead(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY endDate DESC")
    fun getCompletedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'IN_PROGRESS' ORDER BY startDate DESC")
    fun getInProgressBooks(): Flow<List<Book>>

    @Query("""
        SELECT * FROM books 
        WHERE status != 'COMPLETED' 
        AND (:author IS NULL OR :author = '' OR 
             author1 = :author OR author2 = :author OR author3 = :author OR author4 = :author OR author5 = :author)
        AND (:category IS NULL OR :category = '' OR category = :category)
        AND (:titleSearch IS NULL OR :titleSearch = '' OR LOWER(name) LIKE '%' || LOWER(:titleSearch) || '%')
    """)
    suspend fun getBooksToReadFiltered(author: String?, category: String?, titleSearch: String?): List<Book>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Query("SELECT COUNT(*) FROM books WHERE status = 'COMPLETED'")
    fun getTotalBooksRead(): Flow<Int>

    @Query("SELECT COUNT(*) FROM books WHERE status = 'COMPLETED' AND endDate >= :startOfYear")
    suspend fun getBooksReadThisYear(startOfYear: Long): Int

    @Query("SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY endDate DESC")
    suspend fun getAllCompletedBooks(): List<Book>

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    suspend fun getAllBooks(): List<Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE books SET status = :status, startDate = :startDate WHERE id = :id")
    suspend fun updateBookStatus(id: Long, status: BookStatus, startDate: Date?)

    @Query("UPDATE books SET status = :status, endDate = :endDate WHERE id = :id")
    suspend fun markAsCompleted(id: Long, status: BookStatus, endDate: Date?)

    @Query("UPDATE books SET status = :status, currentReadingStartDate = :currentReadingStartDate, totalReadingDays = :totalReadingDays WHERE id = :id")
    suspend fun updateReadingStatus(id: Long, status: BookStatus, currentReadingStartDate: Date?, totalReadingDays: Int)

    @Query("SELECT COUNT(*) FROM books WHERE status != 'COMPLETED'")
    fun getBooksInQueueCount(): Flow<Int>

    @Query("UPDATE books SET ranking = :ranking WHERE id = :id")
    suspend fun updateRanking(id: Long, ranking: Int)

    @Query("UPDATE books SET ranking = ranking + :increment WHERE ranking >= :fromRanking AND ranking <= :toRanking AND id != :excludeId")
    suspend fun shiftRankings(fromRanking: Int, toRanking: Int, increment: Int, excludeId: Long)

    @Query("""
        SELECT DISTINCT author FROM (
            SELECT author1 as author FROM books WHERE status != 'COMPLETED' AND author1 IS NOT NULL AND author1 != ''
            UNION
            SELECT author2 as author FROM books WHERE status != 'COMPLETED' AND author2 IS NOT NULL AND author2 != ''
            UNION
            SELECT author3 as author FROM books WHERE status != 'COMPLETED' AND author3 IS NOT NULL AND author3 != ''
            UNION
            SELECT author4 as author FROM books WHERE status != 'COMPLETED' AND author4 IS NOT NULL AND author4 != ''
            UNION
            SELECT author5 as author FROM books WHERE status != 'COMPLETED' AND author5 IS NOT NULL AND author5 != ''
        ) WHERE author IS NOT NULL AND author != ''
        ORDER BY author ASC
    """)
    fun getAllAuthors(): Flow<List<String>>

    @Query("SELECT DISTINCT category FROM books WHERE status != 'COMPLETED'")
    fun getAllCategoriesForFilter(): Flow<List<String>>
}

