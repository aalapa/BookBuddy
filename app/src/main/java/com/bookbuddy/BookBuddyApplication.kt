package com.bookbuddy

import android.app.Application
import android.util.Log
import com.bookbuddy.data.BookDatabase

class BookBuddyApplication : Application() {
    companion object {
        private const val TAG = "BookBuddy"
    }
    
    val database: BookDatabase by lazy {
        try {
            Log.d(TAG, "Initializing database...")
            val db = BookDatabase.getDatabase(this)
            Log.d(TAG, "Database initialized successfully")
            db
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize database", e)
            e.printStackTrace()
            throw e
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "BookBuddyApplication onCreate")

        // Set up global exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", exception)
            exception.printStackTrace()
            // Call the original default handler
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}

