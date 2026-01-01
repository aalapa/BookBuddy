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
                    repository.insertCategory(com.bookbuddy.data.Category(name = categoryName))
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
                books.forEach { book ->
                    repository.insertBook(book.copy(id = 0)) // Reset ID to let Room auto-generate
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }
}

