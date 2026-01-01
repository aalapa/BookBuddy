package com.bookbuddy.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BookViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
                Log.d("BookBuddy", "Creating BookViewModel")
                @Suppress("UNCHECKED_CAST")
                BookViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } catch (e: Exception) {
            Log.e("BookBuddy", "Error creating ViewModel", e)
            throw e
        }
    }
}

