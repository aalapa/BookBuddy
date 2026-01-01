package com.bookbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#007AFF", // Default to primary blue
    val createdAt: Long = System.currentTimeMillis()
)

