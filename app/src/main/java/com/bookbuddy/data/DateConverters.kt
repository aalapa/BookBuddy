package com.bookbuddy.data

import androidx.room.TypeConverter
import java.util.Date

class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBookStatus(status: BookStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toBookStatus(status: String?): BookStatus? {
        return status?.let {
            try {
                BookStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                // Return default status if invalid value found in database
                BookStatus.NOT_STARTED
            }
        }
    }
}

