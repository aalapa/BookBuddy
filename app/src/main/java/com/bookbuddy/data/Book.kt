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
    val author: String,
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
)

enum class BookStatus {
    NOT_STARTED,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED
}

