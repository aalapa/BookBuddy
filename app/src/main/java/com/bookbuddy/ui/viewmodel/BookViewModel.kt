package com.bookbuddy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bookbuddy.data.Book
import com.bookbuddy.data.BookStatus
import com.bookbuddy.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BookRepository

    val booksToRead: Flow<List<Book>>
    val completedBooks: Flow<List<Book>>
    val inProgressBooks: Flow<List<Book>>
    val totalBooksRead: Flow<Int>
    val booksInQueueCount: Flow<Int>
    val categories: Flow<List<com.bookbuddy.data.Category>>
    val allAuthors: Flow<List<String>>
    val allCategoriesForFilter: Flow<List<String>>

    init {
        android.util.Log.d("BookBuddy", "Initializing BookViewModel...")
        val app = application as? com.bookbuddy.BookBuddyApplication
        if (app == null) {
            android.util.Log.e("BookBuddy", "Application is not BookBuddyApplication!")
            throw IllegalStateException("Application must be BookBuddyApplication")
        }
        try {
            android.util.Log.d("BookBuddy", "Getting database...")
            val database = app.database
            android.util.Log.d("BookBuddy", "Getting DAOs...")
            val bookDao = database.bookDao()
            val categoryDao = database.categoryDao()
            android.util.Log.d("BookBuddy", "Creating repository...")
            repository = BookRepository(bookDao, categoryDao)
            android.util.Log.d("BookBuddy", "Repository created successfully")

            // Initialize Flows
            booksToRead = repository.getBooksToRead()
            completedBooks = repository.getCompletedBooks()
            inProgressBooks = repository.getInProgressBooks()
            totalBooksRead = repository.getTotalBooksRead()
            booksInQueueCount = repository.getBooksInQueueCount()
            categories = repository.getAllCategories()
            allAuthors = repository.getAllAuthors()
            allCategoriesForFilter = repository.getAllCategoriesForFilter()
            
            // Initialize colors for existing categories that don't have colors
            viewModelScope.launch {
                try {
                    repository.initializeCategoryColors()
                } catch (e: Exception) {
                    android.util.Log.e("BookBuddy", "Error initializing category colors", e)
                }
            }
            
            android.util.Log.d("BookBuddy", "BookViewModel initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Failed to initialize ViewModel", e)
            e.printStackTrace()
            throw IllegalStateException("Failed to initialize ViewModel: ${e.message}", e)
        }
    }

    private val _runRate = MutableLiveData<Double>()
    val runRate: LiveData<Double> = _runRate

    private val _booksThisYear = MutableLiveData<Int>()
    val booksThisYear: LiveData<Int> = _booksThisYear

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        // Don't load statistics immediately - let fragments request it when needed
    }

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                _booksThisYear.value = repository.getBooksReadThisYear()
                _runRate.value = repository.calculateRunRate()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun insertBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.insertBook(book)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.updateBook(book)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.deleteBook(book)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun markAsInProgress(bookId: Long) {
        viewModelScope.launch {
            try {
                repository.markAsInProgress(bookId)
                loadStatistics()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun markAsOnHold(bookId: Long) {
        viewModelScope.launch {
            try {
                repository.markAsOnHold(bookId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun markAsCompleted(bookId: Long) {
        viewModelScope.launch {
            try {
                repository.markAsCompleted(bookId)
                loadStatistics()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    suspend fun getFilteredBooks(author: String?, category: String?, titleSearch: String?, sortBy: String): List<Book> {
        return try {
            repository.getBooksToReadFiltered(author, category, titleSearch, sortBy)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            emptyList()
        }
    }

    suspend fun getBookById(id: Long): Book? {
        return try {
            repository.getBookById(id)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            null
        }
    }

    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                val existingCategory = repository.getCategoryByName(categoryName)
                if (existingCategory == null) {
                    // Generate a color for the new category
                    val colorHex = com.bookbuddy.utils.CategoryColorGenerator.generateColorForCategory(categoryName)
                    repository.insertCategory(com.bookbuddy.data.Category(name = categoryName, colorHex = colorHex))
                    android.util.Log.d("BookBuddy", "Added category: $categoryName with color: $colorHex")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun reorderBooks(fromPosition: Int, toPosition: Int, currentBooks: List<Book>) {
        viewModelScope.launch {
            try {
                repository.reorderBook(fromPosition, toPosition, currentBooks)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    suspend fun getAllBooks(): List<Book> {
        return try {
            repository.getAllBooks()
        } catch (e: Exception) {
            _errorMessage.value = e.message
            emptyList()
        }
    }

    fun importBooks(books: List<Book>) {
        viewModelScope.launch {
            try {
                // Extract unique categories from imported books and add them to Category table
                val uniqueCategories = books.map { it.category.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .map { toTitleCase(it) }
                
                uniqueCategories.forEach { categoryName ->
                    val existingCategory = repository.getCategoryByName(categoryName)
                    if (existingCategory == null) {
                        // Generate a color for the imported category
                        val colorHex = com.bookbuddy.utils.CategoryColorGenerator.generateColorForCategory(categoryName)
                        repository.insertCategory(com.bookbuddy.data.Category(name = categoryName, colorHex = colorHex))
                        android.util.Log.d("BookBuddy", "Added category from import: $categoryName with color: $colorHex")
                    }
                }
                
                // Insert books (with title-cased categories for consistency)
                books.forEach { book ->
                    val titleCasedCategory = toTitleCase(book.category.trim())
                    repository.insertBook(book.copy(
                        id = 0, // Reset ID to let Room auto-generate
                        category = titleCasedCategory
                    ))
                }
                
                android.util.Log.d("BookBuddy", "Imported ${books.size} books with ${uniqueCategories.size} categories")
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Error importing books", e)
                _errorMessage.value = e.message
            }
        }
    }
    
    private fun toTitleCase(text: String): String {
        if (text.isEmpty()) return text
        return text.split(" ")
            .joinToString(" ") { word ->
                if (word.isEmpty()) {
                    word
                } else {
                    word.first().uppercaseChar() + word.drop(1).lowercase()
                }
            }
    }
}

