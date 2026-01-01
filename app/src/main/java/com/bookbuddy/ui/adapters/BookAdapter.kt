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
    private val onMarkInProgressClick: (Book) -> Unit,
    private val onMarkOnHoldClick: (Book) -> Unit,
    private val onMarkCompletedClick: (Book) -> Unit,
    private val showActionButtons: Boolean = true,
    private val enableDrag: Boolean = false,
    val onItemMoved: ((Int, Int) -> Unit)? = null
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    // Track expanded position (only one at a time)
    private var expandedBookId: Long? = null
    
    // Track swiped items (bookId to swipe direction)
    private val swipedItems = mutableMapOf<Long, SwipeDirection>()
    
    enum class SwipeDirection {
        LEFT, RIGHT, NONE
    }
    
    fun setSwipeState(bookId: Long, direction: SwipeDirection) {
        if (direction == SwipeDirection.NONE) {
            swipedItems.remove(bookId)
        } else {
            swipedItems[bookId] = direction
        }
    }
    
    fun clearSwipeState(bookId: Long) {
        swipedItems.remove(bookId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = getItem(position)
        holder.bind(book, expandedBookId == book.id)
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
        private val btnMarkInProgress: ImageButton = itemView.findViewById(R.id.btnMarkInProgress)
        private val btnMarkOnHold: ImageButton = itemView.findViewById(R.id.btnMarkOnHold)
        private val progressBar: android.widget.ProgressBar = itemView.findViewById(R.id.progressBar)
        private val cardView: com.google.android.material.card.MaterialCardView = itemView as com.google.android.material.card.MaterialCardView
        private val ivDragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)
        private val llExpandedDetails: View = itemView.findViewById(R.id.llExpandedDetails)
        private val tvExpandedBookName: TextView = itemView.findViewById(R.id.tvExpandedBookName)
        private val tvExpandedAuthor: TextView = itemView.findViewById(R.id.tvExpandedAuthor)
        private val mainContentLayout: View = itemView.findViewById(R.id.mainContentLayout)

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

        fun bind(book: Book, isExpanded: Boolean) {
            tvRanking.text = book.ranking.toString()
            tvBookName.text = book.name
            tvAuthor.text = book.author
            tvCategory.text = book.category
            
            // Set expanded details
            tvExpandedBookName.text = book.name
            tvExpandedAuthor.text = book.author
            
            // Handle expansion state
            llExpandedDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            // Set button click listeners FIRST - these must always work
            // Make sure buttons are always clickable regardless of expansion state
            btnEdit.isClickable = true
            btnEdit.isFocusable = true
            btnEdit.setOnClickListener { onEditClick(book) }
            
            // Set up play/resume button (only shown for NOT_STARTED and ON_HOLD)
            btnMarkInProgress.isClickable = true
            btnMarkInProgress.isFocusable = true
            btnMarkInProgress.setOnClickListener { onMarkInProgressClick(book) }
            
            // Set up pause button (only shown for IN_PROGRESS)
            btnMarkOnHold.isClickable = true
            btnMarkOnHold.isFocusable = true
            btnMarkOnHold.setOnClickListener { onMarkOnHoldClick(book) }
            
            // Initially hide all status buttons - they'll be shown based on book status below
            btnMarkInProgress.visibility = View.GONE
            btnMarkOnHold.visibility = View.GONE
            
            // Helper function to toggle expansion
            val toggleExpansion = {
                // Check current expanded state from adapter (not the captured value)
                val currentlyExpanded = this@BookAdapter.expandedBookId == book.id
                
                // Toggle expansion - if clicking the same card, collapse it
                val newExpandedState = !currentlyExpanded
                
                // Store the previously expanded book ID before updating
                val previouslyExpandedId = this@BookAdapter.expandedBookId
                
                // Update expanded state
                this@BookAdapter.expandedBookId = if (newExpandedState) book.id else null
                
                // If a different card was expanded, collapse it first
                if (previouslyExpandedId != null && previouslyExpandedId != book.id) {
                    // Find the position of the previously expanded item and notify it to update
                    val previousPosition = this@BookAdapter.currentList.indexOfFirst { it.id == previouslyExpandedId }
                    if (previousPosition >= 0) {
                        this@BookAdapter.notifyItemChanged(previousPosition)
                    }
                }
                
                // Animate the expansion/collapse of current card
                // Don't notify adapter for same card - just animate directly
                if (newExpandedState) {
                    llExpandedDetails.visibility = View.VISIBLE
                    llExpandedDetails.alpha = 0f
                    llExpandedDetails.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                } else {
                    llExpandedDetails.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            llExpandedDetails.visibility = View.GONE
                        }
                        .start()
                }
            }
            
            // Set click listener only on the book info area (not buttons)
            val bookInfoArea = itemView.findViewById<ViewGroup>(R.id.bookInfoArea)
            bookInfoArea?.setOnClickListener { toggleExpansion() }
            
            // Also allow clicking on the expanded section to collapse
            // Make sure it doesn't block button clicks
            llExpandedDetails.setOnClickListener { toggleExpansion() }
            llExpandedDetails.isClickable = true
            llExpandedDetails.isFocusable = false
            
            // Ensure buttons layout doesn't intercept clicks
            val buttonsLayout = itemView.findViewById<ViewGroup>(R.id.buttonsLayout)
            buttonsLayout?.isClickable = false
            buttonsLayout?.isFocusable = false
            // Don't set click listener on buttonsLayout - individual buttons handle their own clicks
            
            // Set has book icon - green border with checkmark if owned, gray border with X if not
            if (book.hasBook) {
                ivHasBook.setImageResource(R.drawable.ic_book_owned)
                ivHasBook.clearColorFilter() // No color filter needed, drawable has colors
                ivHasBook.alpha = 1.0f
            } else {
                ivHasBook.setImageResource(R.drawable.ic_book_not_owned)
                ivHasBook.clearColorFilter() // No color filter needed, drawable has colors
                ivHasBook.alpha = 1.0f
            }
            
            // Calculate and display "Added since X days"
            val daysSinceAdded = calculateDaysSince(book.createdAt)
            tvAddedSince.text = when {
                daysSinceAdded == 0 -> "Added today"
                daysSinceAdded == 1 -> "Added 1 day ago"
                else -> "Added $daysSinceAdded days ago"
            }

            // Check if this item is in a swiped state (swipe colors take priority over status colors)
            val swipeState = this@BookAdapter.swipedItems[book.id]
            val isSwiped = swipeState != null && swipeState != SwipeDirection.NONE
            
            // Set progress bar and border/background based on book status
            when (book.status) {
                BookStatus.IN_PROGRESS -> {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = 100 // Show full green bar when reading
                    cardView.strokeWidth = 3
                    cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
                    // Set background - swipe color takes priority
                    if (!isSwiped) {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_background))
                    }
                }
                BookStatus.ON_HOLD -> {
                    progressBar.visibility = View.GONE
                    cardView.strokeWidth = 3
                    cardView.strokeColor = ContextCompat.getColor(itemView.context, R.color.amber)
                    // Set background to light amber (unless swiped)
                    if (!isSwiped) {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.amber_light))
                    }
                }
                else -> {
                    progressBar.visibility = View.GONE
                    cardView.strokeWidth = 0
                    // Reset background to normal (unless swiped)
                    if (!isSwiped) {
                        cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.card_background))
                    }
                }
            }
            
            // Apply swipe background colors (overrides status-based colors)
            if (isSwiped && swipeState != null) {
                when (swipeState) {
                    SwipeDirection.RIGHT -> {
                        // Swiped right (delete) - red background
                        cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FF3B30"))
                    }
                    SwipeDirection.LEFT -> {
                        // Swiped left (complete) - green background
                        cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#34C759"))
                    }
                    SwipeDirection.NONE -> {
                        // Should not happen
                    }
                }
            }

            when (book.status) {
                BookStatus.NOT_STARTED -> {
                    ivStatus.setImageResource(android.R.drawable.ic_menu_info_details)
                    ivStatus.alpha = 0.5f
                    ivStatus.visibility = View.VISIBLE
                    btnMarkInProgress.visibility = if (showActionButtons) View.VISIBLE else View.GONE
                    btnMarkOnHold.visibility = View.GONE
                    llDateInfo.visibility = View.GONE
                    tvReadingSince.visibility = View.GONE
                }
                BookStatus.IN_PROGRESS -> {
                    ivStatus.setImageResource(android.R.drawable.ic_media_play)
                    ivStatus.alpha = 1.0f
                    ivStatus.visibility = View.VISIBLE
                    btnMarkInProgress.visibility = View.GONE // No play button when already reading
                    btnMarkOnHold.visibility = if (showActionButtons) View.VISIBLE else View.GONE // Only pause button
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
            } else {
                btnEdit.visibility = View.GONE
            }

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

