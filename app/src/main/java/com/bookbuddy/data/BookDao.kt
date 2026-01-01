package com.bookbuddy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE status != 'COMPLETED' ORDER BY ranking ASC")
    fun getBooksToRead(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY endDate DESC")
    fun getCompletedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status = 'IN_PROGRESS' ORDER BY startDate DESC")
    fun getInProgressBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE status != 'COMPLETED' AND (:author IS NULL OR author = :author) AND (:category IS NULL OR category = :category)")
    suspend fun getBooksToReadFiltered(author: String?, category: String?): List<Book>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    @Query("SELECT COUNT(*) FROM books WHERE status = 'COMPLETED'")
    fun getTotalBooksRead(): Flow<Int>

    @Query("SELECT COUNT(*) FROM books WHERE status = 'COMPLETED' AND endDate >= :startOfYear")
    suspend fun getBooksReadThisYear(startOfYear: Long): Int

    @Query("SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY endDate DESC")
    suspend fun getAllCompletedBooks(): List<Book>

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    suspend fun getAllBooks(): List<Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE books SET status = :status, startDate = :startDate WHERE id = :id")
    suspend fun updateBookStatus(id: Long, status: BookStatus, startDate: Date?)

    @Query("UPDATE books SET status = :status, endDate = :endDate WHERE id = :id")
    suspend fun markAsCompleted(id: Long, status: BookStatus, endDate: Date?)

    @Query("UPDATE books SET status = :status, currentReadingStartDate = :currentReadingStartDate, totalReadingDays = :totalReadingDays WHERE id = :id")
    suspend fun updateReadingStatus(id: Long, status: BookStatus, currentReadingStartDate: Date?, totalReadingDays: Int)

    @Query("SELECT COUNT(*) FROM books WHERE status != 'COMPLETED'")
    fun getBooksInQueueCount(): Flow<Int>

    @Query("UPDATE books SET ranking = :ranking WHERE id = :id")
    suspend fun updateRanking(id: Long, ranking: Int)

    @Query("UPDATE books SET ranking = ranking + :increment WHERE ranking >= :fromRanking AND ranking <= :toRanking AND id != :excludeId")
    suspend fun shiftRankings(fromRanking: Int, toRanking: Int, increment: Int, excludeId: Long)
}

