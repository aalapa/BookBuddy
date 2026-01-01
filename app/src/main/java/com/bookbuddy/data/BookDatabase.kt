package com.bookbuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(entities = [Book::class, Category::class], version = 4, exportSchema = false)
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
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        BookDatabase::class.java,
                        "book_database"
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onOpen(db: SupportSQLiteDatabase) {
                                super.onOpen(db)
                                // Initialize category colors in a background thread
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val categoryDao = INSTANCE?.categoryDao()
                                        if (categoryDao != null) {
                                            val categoriesWithoutColor = categoryDao.getCategoriesWithoutColor()
                                            if (categoriesWithoutColor.isNotEmpty()) {
                                                android.util.Log.d("BookBuddy", "Found ${categoriesWithoutColor.size} categories without colors, initializing...")
                                                categoriesWithoutColor.forEach { category ->
                                                    val colorHex = com.bookbuddy.utils.CategoryColorGenerator.generateColorForCategory(category.name)
                                                    val updatedCategory = category.copy(colorHex = colorHex)
                                                    categoryDao.updateCategory(updatedCategory)
                                                    android.util.Log.d("BookBuddy", "Assigned color $colorHex to category: ${category.name}")
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("BookBuddy", "Error initializing category colors in callback", e)
                                    }
                                }
                            }
                        })
                        .build()
                    android.util.Log.d("BookBuddy", "Room database created successfully")
                    db
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

