package com.bookbuddy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookbuddy.R
import com.bookbuddy.data.Book
import com.bookbuddy.data.BookStatus
import java.text.SimpleDateFormat
import java.util.*

class BookAdapter(
    private val onEditClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit,
    private val onMarkInProgressClick: (Book) -> Unit,
    private val onMarkOnHoldClick: (Book) -> Unit,
    private val onMarkCompletedClick: (Book) -> Unit,
    private val showActionButtons: Boolean = true,
    private val enableDrag: Boolean = false,
    val onItemMoved: ((Int, Int) -> Unit)? = null
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRanking: TextView = itemView.findViewById(R.id.tvRanking)
        private val tvBookName: TextView = itemView.findViewById(R.id.tvBookName)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val ivCategory: ImageView = itemView.findViewById(R.id.ivCategory)
        private val ivHasBook: ImageView = itemView.findViewById(R.id.ivHasBook)
        private val tvAddedSince: TextView = itemView.findViewById(R.id.tvAddedSince)
        private val tvReadingSince: TextView = itemView.findViewById(R.id.tvReadingSince)
        private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)
        private val tvStartDate: TextView = itemView.findViewById(R.id.tvStartDate)
        private val tvEndDate: TextView = itemView.findViewById(R.id.tvEndDate)
        private val llDateInfo: View = itemView.findViewById(R.id.llDateInfo)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val btnMarkInProgress: ImageButton = itemView.findViewById(R.id.btnMarkInProgress)
        private val btnMarkOnHold: ImageButton = itemView.findViewById(R.id.btnMarkOnHold)
        private val btnMarkCompleted: ImageButton = itemView.findViewById(R.id.btnMarkCompleted)
        private val ivDragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)

        private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

        private fun calculateDaysSince(date: Date): Int {
            val now = Calendar.getInstance()
            val then = Calendar.getInstance().apply { time = date }
            
            // Reset time to midnight for accurate day calculation
            now.set(Calendar.HOUR_OF_DAY, 0)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            now.set(Calendar.MILLISECOND, 0)
            
            then.set(Calendar.HOUR_OF_DAY, 0)
            then.set(Calendar.MINUTE, 0)
            then.set(Calendar.SECOND, 0)
            then.set(Calendar.MILLISECOND, 0)
            
            val diffInMillis = now.timeInMillis - then.timeInMillis
            return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
        }

        fun bind(book: Book) {
            tvRanking.text = book.ranking.toString()
            tvBookName.text = book.name
            tvAuthor.text = book.author
            tvCategory.text = book.category
            
            // Set has book icon - green checkmark circle if owned, gray X circle if not
            if (book.hasBook) {
                ivHasBook.setImageResource(android.R.drawable.checkbox_on_background)
                ivHasBook.setColorFilter(ContextCompat.getColor(itemView.context, R.color.success))
                ivHasBook.alpha = 1.0f
            } else {
                ivHasBook.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                ivHasBook.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                ivHasBook.alpha = 0.7f
            }
            
            // Calculate and display "Added since X days"
            val daysSinceAdded = calculateDaysSince(book.createdAt)
            tvAddedSince.text = when {
                daysSinceAdded == 0 -> "Added today"
                daysSinceAdded == 1 -> "Added 1 day ago"
                else -> "Added $daysSinceAdded days ago"
            }

            when (book.status) {
                BookStatus.NOT_STARTED -> {
                    ivStatus.setImageResource(android.R.drawable.ic_menu_info_details)
                    ivStatus.alpha = 0.5f
                    ivStatus.visibility = View.VISIBLE
                    btnMarkInProgress.visibility = if (showActionButtons) View.VISIBLE else View.GONE
                    btnMarkCompleted.visibility = View.GONE
                    llDateInfo.visibility = View.GONE
                    tvReadingSince.visibility = View.GONE
                }
                BookStatus.IN_PROGRESS -> {
                    ivStatus.setImageResource(android.R.drawable.ic_media_play)
                    ivStatus.alpha = 1.0f
                    ivStatus.visibility = View.VISIBLE
                    btnMarkInProgress.visibility = View.GONE
                    btnMarkOnHold.visibility = if (showActionButtons) View.VISIBLE else View.GONE
                    btnMarkCompleted.visibility = if (showActionButtons) View.VISIBLE else View.GONE
                    llDateInfo.visibility = View.VISIBLE
                    
                    // Calculate total reading days: accumulated + current session
                    val currentSessionDays = book.currentReadingStartDate?.let { 
                        calculateDaysSince(it) 
                    } ?: 0
                    val totalDays = book.totalReadingDays + currentSessionDays
                    
                    tvReadingSince.text = when {
                        totalDays == 0 -> "Reading today"
                        totalDays == 1 -> "Reading 1 day"
                        else -> "Reading $totalDays days"
                    }
                    tvReadingSince.visibility = View.VISIBLE
                    
                    book.currentReadingStartDate?.let {
                        tvStartDate.text = "S: ${dateFormat.format(it)}"
                    } ?: run {
                        tvStartDate.text = ""
                    }
                    tvEndDate.visibility = View.GONE
                }
                BookStatus.ON_HOLD -> {
                    ivStatus.setImageResource(android.R.drawable.ic_media_pause)
                    ivStatus.alpha = 0.7f
                    ivStatus.visibility = View.VISIBLE
                    btnMarkInProgress.visibility = if (showActionButtons) View.VISIBLE else View.GONE // Resume button
                    btnMarkOnHold.visibility = View.GONE
                    btnMarkCompleted.visibility = View.GONE
                    llDateInfo.visibility = View.GONE
                    
                    // Show total reading days accumulated so far
                    if (book.totalReadingDays > 0) {
                        tvReadingSince.text = when {
                            book.totalReadingDays == 1 -> "Read 1 day total"
                            else -> "Read ${book.totalReadingDays} days total"
                        }
                        tvReadingSince.visibility = View.VISIBLE
                    } else {
                        tvReadingSince.visibility = View.GONE
                    }
                }
                BookStatus.COMPLETED -> {
                    ivStatus.setImageResource(android.R.drawable.checkbox_on_background)
                    ivStatus.setColorFilter(ContextCompat.getColor(itemView.context, R.color.success))
                    ivStatus.alpha = 1.0f
                    ivStatus.visibility = View.VISIBLE
                    btnMarkInProgress.visibility = View.GONE
                    btnMarkCompleted.visibility = View.GONE
                    llDateInfo.visibility = View.VISIBLE
                    tvReadingSince.visibility = View.GONE
                    book.startDate?.let {
                        tvStartDate.text = "S: ${dateFormat.format(it)}"
                    } ?: run {
                        tvStartDate.text = ""
                    }
                    book.endDate?.let {
                        tvEndDate.text = "C: ${dateFormat.format(it)}"
                        tvEndDate.visibility = View.VISIBLE
                    } ?: run {
                        tvEndDate.visibility = View.GONE
                    }
                }
            }

            if (showActionButtons) {
                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
            } else {
                btnEdit.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }

            btnEdit.setOnClickListener { onEditClick(book) }
            btnDelete.setOnClickListener { onDeleteClick(book) }
            btnMarkInProgress.setOnClickListener { onMarkInProgressClick(book) }
            btnMarkOnHold.setOnClickListener { onMarkOnHoldClick(book) }
            btnMarkCompleted.setOnClickListener { onMarkCompletedClick(book) }

            // Show drag handle if drag is enabled
            if (enableDrag) {
                ivDragHandle.visibility = View.VISIBLE
            } else {
                ivDragHandle.visibility = View.GONE
            }
        }
    }

    fun getItemAt(position: Int): Book? {
        return if (position >= 0 && position < itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}

