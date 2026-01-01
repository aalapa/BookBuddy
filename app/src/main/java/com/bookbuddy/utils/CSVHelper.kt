package com.bookbuddy.utils

import com.bookbuddy.data.Book
import com.bookbuddy.data.BookStatus
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object CSVHelper {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportBooksToCSV(books: List<Book>, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        
        // Write CSV header
        writer.append("ID,Name,Author,Category,Ranking,HasBook,Status,StartDate,EndDate,CreatedAt,TotalReadingDays,CurrentReadingStartDate\n")
        
        // Write book data
        books.forEach { book ->
            writer.append("${book.id},")
            writer.append("\"${book.name.replace("\"", "\"\"")}\",")
            writer.append("\"${book.author.replace("\"", "\"\"")}\",")
            writer.append("\"${book.category.replace("\"", "\"\"")}\",")
            writer.append("${book.ranking},")
            writer.append("${book.hasBook},")
            writer.append("${book.status.name},")
            writer.append("${book.startDate?.let { dateFormat.format(it) } ?: ""},")
            writer.append("${book.endDate?.let { dateFormat.format(it) } ?: ""},")
            writer.append("${dateFormat.format(book.createdAt)},")
            writer.append("${book.totalReadingDays},")
            writer.append("${book.currentReadingStartDate?.let { dateFormat.format(it) } ?: ""}\n")
        }
        
        writer.flush()
        writer.close()
    }

    fun importBooksFromCSV(inputStream: InputStream): List<Book> {
        val books = mutableListOf<Book>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        
        // Skip header line
        reader.readLine()
        
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            try {
                val book = parseCSVLine(line!!)
                if (book != null) {
                    books.add(book)
                }
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Error parsing CSV line: $line", e)
            }
        }
        
        reader.close()
        return books
    }

    private fun parseCSVLine(line: String): Book? {
        val fields = parseCSVFields(line)
        if (fields.size < 12) return null

        return try {
            Book(
                id = fields[0].toLongOrNull() ?: 0L,
                name = fields[1].trim('"'),
                author = fields[2].trim('"'),
                category = fields[3].trim('"'),
                ranking = fields[4].toIntOrNull() ?: 1,
                hasBook = fields[5].toBoolean(),
                status = BookStatus.valueOf(fields[6]),
                startDate = fields[7].takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it) },
                endDate = fields[8].takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it) },
                createdAt = fields[9].takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it) } ?: Date(),
                totalReadingDays = fields[10].toIntOrNull() ?: 0,
                currentReadingStartDate = fields[11].takeIf { it.isNotEmpty() }?.let { dateFormat.parse(it) }
            )
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Error creating book from CSV", e)
            null
        }
    }

    private fun parseCSVFields(line: String): List<String> {
        val fields = mutableListOf<String>()
        var currentField = StringBuilder()
        var insideQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> insideQuotes = !insideQuotes
                char == ',' && !insideQuotes -> {
                    fields.add(currentField.toString())
                    currentField = StringBuilder()
                }
                else -> currentField.append(char)
            }
        }
        fields.add(currentField.toString()) // Add last field
        
        return fields
    }
}

