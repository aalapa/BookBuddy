package com.bookbuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.util.Date

@Database(entities = [Book::class, Category::class], version = 2, exportSchema = false)
@TypeConverters(DateConverters::class)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(context: Context): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: try {
                    android.util.Log.d("BookBuddy", "Creating Room database...")
                    Room.databaseBuilder(
                        context.applicationContext,
                        BookDatabase::class.java,
                        "book_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build().also {
                            android.util.Log.d("BookBuddy", "Room database created successfully")
                        }
                } catch (e: Exception) {
                    android.util.Log.e("BookBuddy", "Error creating Room database", e)
                    e.printStackTrace()
                    throw e
                }
                INSTANCE = instance
                instance
            }
        }
    }
}

