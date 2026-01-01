package com.bookbuddy.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bookbuddy.R
import com.bookbuddy.data.Book
import com.bookbuddy.databinding.FragmentBooksToReadBinding
import com.bookbuddy.ui.adapters.BookAdapter
import com.bookbuddy.ui.viewmodel.BookViewModel
import com.bookbuddy.ui.viewmodel.BookViewModelFactory
import kotlinx.coroutines.launch

class BooksToReadFragment : Fragment() {
    private var _binding: FragmentBooksToReadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookViewModel by viewModels {
        try {
            android.util.Log.d("BookBuddy", "Creating ViewModelFactory in BooksToReadFragment")
            BookViewModelFactory(requireActivity().application)
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Error creating ViewModelFactory", e)
            throw e
        }
    }
    private lateinit var adapter: BookAdapter
    private var selectedAuthor: String? = null
    private var selectedCategory: String? = null
    private var sortBy: String = "ranking" // ranking, date_asc, date_desc
    private var isUpdatingFilterProgrammatically = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksToReadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            android.util.Log.d("BookBuddy", "BooksToReadFragment onViewCreated")
            setupToolbar()
            setupRecyclerView()
            android.util.Log.d("BookBuddy", "RecyclerView setup complete")
            setupFilterAndSort()
            android.util.Log.d("BookBuddy", "Filter and sort setup complete")
            setupObservers()
            android.util.Log.d("BookBuddy", "Observers setup complete")
            setupClickListeners()
            android.util.Log.d("BookBuddy", "Click listeners setup complete")
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Error in BooksToReadFragment onViewCreated", e)
            e.printStackTrace()
        }
    }

    private fun setupToolbar() {
        val activity = requireActivity() as? androidx.appcompat.app.AppCompatActivity
        activity?.setSupportActionBar(binding.toolbar)
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_more)
        
        binding.toolbar.setNavigationOnClickListener {
            val drawerLayout = activity?.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
            drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
        }
    }

    private fun setupRecyclerView() {
        try {
            android.util.Log.d("BookBuddy", "Setting up RecyclerView...")
            adapter = BookAdapter(
                onEditClick = { book -> navigateToEditBook(book.id) },
                onMarkInProgressClick = { book -> viewModel.markAsInProgress(book.id) },
                onMarkOnHoldClick = { book -> viewModel.markAsOnHold(book.id) },
                onMarkCompletedClick = { book -> viewModel.markAsCompleted(book.id) },
                showActionButtons = true,
                enableDrag = true,
                onItemMoved = { fromPosition, toPosition ->
                    // Get current list from adapter
                    val currentList = adapter.currentList.toList()
                    if (fromPosition >= 0 && toPosition >= 0 && 
                        fromPosition < currentList.size && toPosition < currentList.size) {
                        viewModel.reorderBooks(fromPosition, toPosition, currentList)
                    }
                }
            )

            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = adapter

            // Setup drag and drop
            val itemTouchHelper = ItemTouchHelper(createItemTouchHelperCallback())
            itemTouchHelper.attachToRecyclerView(binding.recyclerView)

            android.util.Log.d("BookBuddy", "RecyclerView setup successful")
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Error setting up RecyclerView", e)
            e.printStackTrace()
            throw e
        }
    }

    private fun createItemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                    return false
                }

                // Update the adapter's list
                val currentList = adapter.currentList.toMutableList()
                val movedItem = currentList.removeAt(fromPosition)
                currentList.add(toPosition, movedItem)
                adapter.submitList(currentList)

                // Notify the callback
                adapter.onItemMoved?.invoke(fromPosition, toPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return

                val book = adapter.getItemAt(position) ?: return

                when (direction) {
                    ItemTouchHelper.RIGHT -> {
                        // Swipe right = Delete
                        adapter.setSwipeState(book.id, BookAdapter.SwipeDirection.RIGHT)
                        adapter.notifyItemChanged(position) // Update to show red background
                        showDeleteConfirmation(book, position)
                    }
                    ItemTouchHelper.LEFT -> {
                        // Swipe left = Mark as Complete
                        adapter.setSwipeState(book.id, BookAdapter.SwipeDirection.LEFT)
                        adapter.notifyItemChanged(position) // Update to show green background
                        showCompleteConfirmation(book, position)
                    }
                }
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val background = android.graphics.drawable.ColorDrawable()
                    val icon: android.graphics.drawable.Drawable
                    val iconMargin: Int
                    val iconTop: Int
                    val iconBottom: Int
                    val iconLeft: Int
                    val iconRight: Int

                    when {
                        dX > 0 -> {
                            // Swiping right (delete) - red background
                            background.color = android.graphics.Color.parseColor("#FF3B30")
                            icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)!!
                            icon.setTint(android.graphics.Color.WHITE)
                            iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                            iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                            iconBottom = iconTop + icon.intrinsicHeight
                            iconLeft = itemView.left + iconMargin
                            iconRight = iconLeft + icon.intrinsicWidth
                            background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                        }
                        dX < 0 -> {
                            // Swiping left (complete) - green background
                            background.color = android.graphics.Color.parseColor("#34C759")
                            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_checkmark_circle)!!
                            icon.setTint(android.graphics.Color.WHITE)
                            iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                            iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                            iconBottom = iconTop + icon.intrinsicHeight
                            iconRight = itemView.right - iconMargin
                            iconLeft = iconRight - icon.intrinsicWidth
                            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                        }
                        else -> {
                            background.setBounds(0, 0, 0, 0)
                            icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete)!!
                            iconMargin = 0
                            iconTop = 0
                            iconBottom = 0
                            iconLeft = 0
                            iconRight = 0
                        }
                    }

                    background.draw(c)
                    if (dX != 0f) {
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.draw(c)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    private fun showCompleteConfirmation(book: Book, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.mark_as_completed)
            .setMessage(getString(R.string.complete_confirmation, book.name, book.author))
            .setPositiveButton(R.string.mark_as_completed) { dialog, _ ->
                // Clear swipe state and let the item be removed
                adapter.clearSwipeState(book.id)
                viewModel.markAsCompleted(book.id)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                // Restore the item - clear swipe state
                adapter.clearSwipeState(book.id)
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .setOnDismissListener {
                // Also restore if dialog is dismissed without button press
                adapter.clearSwipeState(book.id)
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private var currentBooks: List<Book> = emptyList()

    private fun setupFilterAndSort() {
        // Set threshold for AutoCompleteTextView to show suggestions immediately
        binding.actvAuthorFilter.threshold = 0
        binding.actvCategoryFilter.threshold = 0
        
        // Setup author filter with multiple listeners
        binding.actvAuthorFilter.setOnItemClickListener { _, _, position, _ ->
            android.util.Log.d("BookBuddy", "Author filter clicked, position: $position")
            isUpdatingFilterProgrammatically = true
            val authors = listOf("All") + currentBooks.map { it.author }.distinct().sorted()
            if (position < authors.size) {
                selectedAuthor = if (position == 0) null else authors[position]
                android.util.Log.d("BookBuddy", "Selected author: $selectedAuthor")
                binding.actvAuthorFilter.setText(selectedAuthor ?: "All", false)
                isUpdatingFilterProgrammatically = false
                applyFiltersAndSort()
            } else {
                isUpdatingFilterProgrammatically = false
            }
        }
        
        // Add TextWatcher for author filter
        binding.actvAuthorFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingFilterProgrammatically) return
                val text = s?.toString()?.trim() ?: ""
                android.util.Log.d("BookBuddy", "Author text changed: '$text'")
                if (text.isEmpty() || text == "All") {
                    selectedAuthor = null
                } else {
                    val authors = currentBooks.map { it.author }.distinct().sorted()
                    if (authors.contains(text)) {
                        selectedAuthor = text
                    } else {
                        selectedAuthor = null
                    }
                }
                applyFiltersAndSort()
            }
        })

        // Setup category filter listener
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                val categoryNames = categories.map { it.name }.sorted()
                val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("All") + categoryNames)
                binding.actvCategoryFilter.setAdapter(categoryAdapter)
                
                if (binding.actvCategoryFilter.tag == null) {
                    binding.actvCategoryFilter.tag = "listener_set"
                    
                    binding.actvCategoryFilter.setOnItemClickListener { _, _, position, _ ->
                        android.util.Log.d("BookBuddy", "Category filter clicked, position: $position")
                        isUpdatingFilterProgrammatically = true
                        val allCategories = listOf("All") + categoryNames
                        if (position < allCategories.size) {
                            selectedCategory = if (position == 0) null else allCategories[position]
                            android.util.Log.d("BookBuddy", "Selected category: $selectedCategory")
                            binding.actvCategoryFilter.setText(selectedCategory ?: "All", false)
                            isUpdatingFilterProgrammatically = false
                            applyFiltersAndSort()
                        } else {
                            isUpdatingFilterProgrammatically = false
                        }
                    }
                    
                    // Add TextWatcher for category filter
                    binding.actvCategoryFilter.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            if (isUpdatingFilterProgrammatically) return
                            val text = s?.toString()?.trim() ?: ""
                            android.util.Log.d("BookBuddy", "Category text changed: '$text'")
                            if (text.isEmpty() || text == "All") {
                                selectedCategory = null
                            } else {
                                if (categoryNames.contains(text)) {
                                    selectedCategory = text
                                } else {
                                    selectedCategory = null
                                }
                            }
                            applyFiltersAndSort()
                        }
                    })
                }
            }
        }

        // Setup sort button
        binding.btnSort.setOnClickListener {
            showSortMenu()
        }
    }

    private fun showSortMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.btnSort)
        popupMenu.menu.add(0, 0, 0, "By Ranking (Default)")
        popupMenu.menu.add(0, 1, 1, "Date Added (Oldest First)")
        popupMenu.menu.add(0, 2, 2, "Date Added (Newest First)")
        
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            sortBy = when (item.itemId) {
                0 -> "ranking"
                1 -> "date_asc"
                2 -> "date_desc"
                else -> "ranking"
            }
            applyFiltersAndSort()
            true
        }
        popupMenu.show()
    }

    private fun applyFiltersAndSort() {
        if (!::adapter.isInitialized || _binding == null) {
            android.util.Log.d("BookBuddy", "Adapter or binding not ready, skipping filter")
            return
        }
        
        android.util.Log.d("BookBuddy", "Applying filters - Author: $selectedAuthor, Category: $selectedCategory, Sort: $sortBy")
        android.util.Log.d("BookBuddy", "Current books count: ${currentBooks.size}")
        
        if (currentBooks.isEmpty()) {
            android.util.Log.d("BookBuddy", "No books to filter")
            adapter.submitList(emptyList())
            binding.emptyStateText.visibility = View.VISIBLE
            return
        }
        
        var filtered = currentBooks.toList() // Create a copy
        
        // Apply author filter
        if (selectedAuthor != null && selectedAuthor!!.isNotEmpty() && selectedAuthor != "All") {
            filtered = filtered.filter { it.author == selectedAuthor }
            android.util.Log.d("BookBuddy", "After author filter ($selectedAuthor): ${filtered.size} books")
        } else {
            android.util.Log.d("BookBuddy", "No author filter applied")
        }
        
        // Apply category filter
        if (selectedCategory != null && selectedCategory!!.isNotEmpty() && selectedCategory != "All") {
            filtered = filtered.filter { it.category == selectedCategory }
            android.util.Log.d("BookBuddy", "After category filter ($selectedCategory): ${filtered.size} books")
        } else {
            android.util.Log.d("BookBuddy", "No category filter applied")
        }
        
        // Apply sort
        filtered = when (sortBy) {
            "date_asc" -> filtered.sortedBy { it.createdAt }
            "date_desc" -> filtered.sortedByDescending { it.createdAt }
            else -> filtered.sortedBy { it.ranking } // Default: by ranking
        }
        
        android.util.Log.d("BookBuddy", "Final filtered count: ${filtered.size}")
        
        adapter.submitList(filtered)
        binding.emptyStateText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupObservers() {
        try {
            android.util.Log.d("BookBuddy", "Setting up observers...")
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    android.util.Log.d("BookBuddy", "Starting to collect booksToRead...")
                    viewModel.booksToRead.collect { books ->
                        try {
                            android.util.Log.d("BookBuddy", "Received ${books.size} books")
                            currentBooks = books
                            
                            // Update author filter dropdown
                            val authors = books.map { it.author }.distinct().sorted()
                            val authorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("All") + authors)
                            binding.actvAuthorFilter.setAdapter(authorAdapter)
                            
                            // Initialize with "All" if empty
                            if (binding.actvAuthorFilter.text.toString().isEmpty()) {
                                binding.actvAuthorFilter.setText("All", false)
                                selectedAuthor = null
                            }
                            
                            // Apply filters and sort
                            applyFiltersAndSort()
                        } catch (e: Exception) {
                            android.util.Log.e("BookBuddy", "Error updating book list", e)
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BookBuddy", "Error collecting booksToRead", e)
                    e.printStackTrace()
                }
            }
            android.util.Log.d("BookBuddy", "Observers setup successful")
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Error in setupObservers", e)
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddBook.setOnClickListener { view ->
            android.util.Log.d("BookBuddy", "FAB clicked! View: $view")
            try {
                navigateToAddBook()
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Exception in FAB click", e)
                e.printStackTrace()
            }
        }
        android.util.Log.d("BookBuddy", "FAB click listener set up. FAB: ${binding.fabAddBook}")
        
        // Also check if FAB is clickable
        binding.fabAddBook.isClickable = true
        binding.fabAddBook.isFocusable = true
    }

    private fun navigateToAddBook() {
        try {
            android.util.Log.d("BookBuddy", "Navigating to add book screen")
            val navController = findNavController()
            android.util.Log.d("BookBuddy", "NavController found: $navController")
            
            val bundle = Bundle().apply {
                putLong("bookId", -1L)
            }
            
            android.util.Log.d("BookBuddy", "Action ID: ${R.id.action_booksToReadFragment_to_addEditBookFragment}")
            navController.navigate(R.id.action_booksToReadFragment_to_addEditBookFragment, bundle)
            android.util.Log.d("BookBuddy", "Navigation successful")
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Error navigating to add book", e)
            e.printStackTrace()
            // Show error to user
            android.widget.Toast.makeText(requireContext(), "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToEditBook(bookId: Long) {
        try {
            val bundle = Bundle().apply {
                putLong("bookId", bookId)
            }
            findNavController().navigate(R.id.action_booksToReadFragment_to_addEditBookFragment, bundle)
        } catch (e: Exception) {
            android.util.Log.e("BookBuddy", "Error navigating to edit book", e)
            e.printStackTrace()
        }
    }

    private fun showDeleteConfirmation(book: Book, position: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_book)
            .setMessage(getString(R.string.delete_confirmation, book.name, book.author))
            .setPositiveButton(R.string.delete) { dialog, _ ->
                // Clear swipe state and let the item be removed
                adapter.clearSwipeState(book.id)
                viewModel.deleteBook(book)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                // Restore the item - clear swipe state
                adapter.clearSwipeState(book.id)
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .setOnDismissListener {
                // Also restore if dialog is dismissed without button press
                adapter.clearSwipeState(book.id)
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

