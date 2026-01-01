package com.bookbuddy.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "author")
    val author: String = "", // Kept for backward compatibility and display
    @ColumnInfo(name = "author1")
    val author1: String? = null,
    @ColumnInfo(name = "author2")
    val author2: String? = null,
    @ColumnInfo(name = "author3")
    val author3: String? = null,
    @ColumnInfo(name = "author4")
    val author4: String? = null,
    @ColumnInfo(name = "author5")
    val author5: String? = null,
    val category: String,
    val ranking: Int,
    val hasBook: Boolean,
    @ColumnInfo(name = "status")
    val status: BookStatus,
    @ColumnInfo(name = "startDate")
    val startDate: Date? = null,
    @ColumnInfo(name = "endDate")
    val endDate: Date? = null,
    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "totalReadingDays")
    val totalReadingDays: Int = 0,
    @ColumnInfo(name = "currentReadingStartDate")
    val currentReadingStartDate: Date? = null
) {
    // Helper function to get all authors as a list
    fun getAllAuthors(): List<String> {
        return listOfNotNull(author1, author2, author3, author4, author5).filter { it.isNotBlank() }
    }
    
    // Helper function to get display author string
    fun getDisplayAuthor(): String {
        val authors = getAllAuthors()
        return if (authors.isNotEmpty()) {
            authors.joinToString(", ")
        } else {
            author // Fallback to old author field
        }
    }
}

enum class BookStatus {
    NOT_STARTED,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED
}

