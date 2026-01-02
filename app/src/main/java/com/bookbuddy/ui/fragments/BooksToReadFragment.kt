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
    private var searchTitle: String = ""
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
            setupSearch()
            android.util.Log.d("BookBuddy", "Search setup complete")
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
        activity?.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger)
        
        binding.toolbar.setNavigationOnClickListener {
            val drawerLayout = activity?.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
            drawerLayout?.openDrawer(androidx.core.view.GravityCompat.END)
        }
        
        // Setup search icon click listener
        binding.ivSearchIcon.setOnClickListener {
            toggleSearchBar()
        }
    }
    
    private fun toggleSearchBar() {
        val isVisible = binding.tilSearch.visibility == View.VISIBLE
        if (isVisible) {
            // Hide search bar
            binding.tilSearch.visibility = View.GONE
            // Clear search text
            binding.etSearch.setText("")
            searchTitle = ""
            applyFiltersAndSort()
            // Hide keyboard
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        } else {
            // Show search bar
            binding.tilSearch.visibility = View.VISIBLE
            // Focus on search field and show keyboard
            binding.etSearch.requestFocus()
            binding.etSearch.post {
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun setupRecyclerView() {
        try {
            android.util.Log.d("BookBuddy", "Setting up RecyclerView...")
            adapter = BookAdapter(
                onEditClick = { book -> navigateToEditBook(book.id) },
                onMarkInProgressClick = { book -> viewModel.markAsInProgress(book.id) },
                onMarkOnHoldClick = { book -> viewModel.markAsOnHold(book.id) },
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
        binding.actvAuthorFilter.threshold = 1 // Show suggestions after 1 character
        binding.actvCategoryFilter.threshold = 1
        
        // Set dropdown background to match app theme (rounded corners, white background)
        binding.actvAuthorFilter.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dropdown_popup_background)
        )
        binding.actvCategoryFilter.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dropdown_popup_background)
        )
        
        // Setup author filter - use ViewModel to get all authors with filterable adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allAuthors.collect { authors ->
                val authorList = listOf("All") + authors
                // Use filterable ArrayAdapter for autocomplete with custom layout
                val authorAdapter = object : ArrayAdapter<String>(
                    requireContext(),
                    R.layout.dropdown_item_filter,
                    authorList
                ) {
                    override fun getFilter(): android.widget.Filter {
                        return object : android.widget.Filter() {
                            override fun performFiltering(constraint: CharSequence?): FilterResults {
                                val results = FilterResults()
                                val filteredList = mutableListOf<String>()
                                
                                val filterPattern = if (constraint.isNullOrBlank()) {
                                    ""
                                } else {
                                    constraint.toString().lowercase().trim()
                                }
                                
                                if (filterPattern.isEmpty()) {
                                    // Show all when empty
                                    filteredList.addAll(authorList)
                                } else {
                                    // Always include "All" option
                                    filteredList.add("All")
                                    // Filter based on pattern
                                    for (author in authorList) {
                                        if (author != "All" && author.lowercase().contains(filterPattern)) {
                                            filteredList.add(author)
                                        }
                                    }
                                }
                                
                                results.values = filteredList
                                results.count = filteredList.size
                                android.util.Log.d("BookBuddy", "Author filter: pattern='$filterPattern', results=${filteredList.size}")
                                return results
                            }
                            
                            @Suppress("UNCHECKED_CAST")
                            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                                clear()
                                if (results != null && results.count > 0) {
                                    addAll(results.values as List<String>)
                                    notifyDataSetChanged()
                                    android.util.Log.d("BookBuddy", "Author adapter updated with ${results.count} items")
                                } else {
                                    notifyDataSetInvalidated()
                                }
                            }
                        }
                    }
                }
                
                binding.actvAuthorFilter.setAdapter(authorAdapter)
                
                if (binding.actvAuthorFilter.tag == null) {
                    binding.actvAuthorFilter.tag = "listener_set"
                    
                    // Clear "All" when clicked/focused
                    binding.actvAuthorFilter.setOnClickListener {
                        if (binding.actvAuthorFilter.text.toString() == "All") {
                            binding.actvAuthorFilter.setText("")
                        }
                        // Show dropdown with all options
                        binding.actvAuthorFilter.post {
                            binding.actvAuthorFilter.showDropDown()
                        }
                    }
                    
                    binding.actvAuthorFilter.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            if (binding.actvAuthorFilter.text.toString() == "All") {
                                binding.actvAuthorFilter.setText("")
                            }
                            // Show dropdown with all options when focused
                            binding.actvAuthorFilter.post {
                                binding.actvAuthorFilter.showDropDown()
                            }
                        }
                    }
                    
                    binding.actvAuthorFilter.setOnItemClickListener { parent, view, position, id ->
                        android.util.Log.d("BookBuddy", "Author filter clicked, position: $position")
                        isUpdatingFilterProgrammatically = true
                        // Get the selected item from the parent adapter (the actual adapter on the view)
                        // instead of the closure variable to avoid stale adapter references
                        val selectedItem = parent.getItemAtPosition(position) as? String
                        if (selectedItem != null) {
                            selectedAuthor = if (selectedItem == "All") null else selectedItem
                            android.util.Log.d("BookBuddy", "Selected author: $selectedAuthor")
                            // Dismiss dropdown first, before setting text
                            binding.actvAuthorFilter.dismissDropDown()
                            binding.actvAuthorFilter.setText(selectedItem, false)
                            binding.actvAuthorFilter.clearFocus()
                            // Also hide keyboard if visible
                            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                            imm.hideSoftInputFromWindow(binding.actvAuthorFilter.windowToken, 0)
                            isUpdatingFilterProgrammatically = false
                            applyFiltersAndSort()
                        } else {
                            isUpdatingFilterProgrammatically = false
                        }
                    }
                    
                    // Add TextWatcher - show dropdown as user types
                    binding.actvAuthorFilter.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            if (isUpdatingFilterProgrammatically) {
                                // Don't show dropdown when updating programmatically
                                return
                            }
                            val text = s?.toString()?.trim() ?: ""
                            android.util.Log.d("BookBuddy", "Author text changed: '$text'")
                            
                            // Only show dropdown if field has focus (user is typing)
                            if (binding.actvAuthorFilter.hasFocus()) {
                                binding.actvAuthorFilter.post {
                                    if (text.isNotEmpty() && binding.actvAuthorFilter.adapter != null) {
                                        binding.actvAuthorFilter.showDropDown()
                                    } else if (text.isEmpty()) {
                                        // Show all options when empty
                                        binding.actvAuthorFilter.showDropDown()
                                        selectedAuthor = null
                                        applyFiltersAndSort()
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }

        // Setup category filter - use ViewModel to get all categories with filterable adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allCategoriesForFilter.collect { categoryNames ->
                val categoryList = listOf("All") + categoryNames.sorted()
                android.util.Log.d("BookBuddy", "Category list updated: ${categoryList.size} items - $categoryList")
                
                // Get current text and selection before updating adapter
                val currentText = binding.actvCategoryFilter.text?.toString() ?: ""
                val wasAll = currentText == "All" || currentText.isEmpty()
                
                // Use filterable ArrayAdapter for autocomplete with custom layout
                val categoryAdapter = object : ArrayAdapter<String>(
                    requireContext(),
                    R.layout.dropdown_item_filter,
                    categoryList
                ) {
                    override fun getFilter(): android.widget.Filter {
                        return object : android.widget.Filter() {
                            override fun performFiltering(constraint: CharSequence?): FilterResults {
                                val results = FilterResults()
                                val filteredList = mutableListOf<String>()
                                
                                val filterPattern = if (constraint.isNullOrBlank()) {
                                    ""
                                } else {
                                    constraint.toString().lowercase().trim()
                                }
                                
                                // Always use the categoryList from the closure (latest data)
                                if (filterPattern.isEmpty()) {
                                    // Show all when empty - use the full category list
                                    filteredList.addAll(categoryList)
                                } else {
                                    // Always include "All" option
                                    filteredList.add("All")
                                    // Filter based on pattern
                                    for (category in categoryList) {
                                        if (category != "All" && category.lowercase().contains(filterPattern)) {
                                            filteredList.add(category)
                                        }
                                    }
                                }
                                
                                results.values = filteredList
                                results.count = filteredList.size
                                android.util.Log.d("BookBuddy", "Category filter: pattern='$filterPattern', source=${categoryList.size}, results=${filteredList.size}")
                                return results
                            }
                            
                            @Suppress("UNCHECKED_CAST")
                            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                                clear()
                                if (results != null && results.count > 0) {
                                    addAll(results.values as List<String>)
                                    notifyDataSetChanged()
                                    android.util.Log.d("BookBuddy", "Category adapter updated with ${results.count} items")
                                } else {
                                    notifyDataSetInvalidated()
                                }
                            }
                        }
                    }
                }
                
                // Set the new adapter
                binding.actvCategoryFilter.setAdapter(categoryAdapter)
                
                // Reset filter state and restore text
                binding.actvCategoryFilter.post {
                    // Clear any active filter by filtering with empty string
                    categoryAdapter.filter.filter("")
                    
                    // Restore the text - if it was "All" or empty, set to "All"
                    if (wasAll) {
                        binding.actvCategoryFilter.setText("All", false)
                    } else if (currentText.isNotEmpty() && categoryList.contains(currentText)) {
                        // Only restore if the category still exists
                        binding.actvCategoryFilter.setText(currentText, false)
                    } else {
                        // Category was deleted, reset to "All"
                        binding.actvCategoryFilter.setText("All", false)
                        selectedCategory = null
                        applyFiltersAndSort()
                    }
                }
                
                if (binding.actvCategoryFilter.tag == null) {
                    binding.actvCategoryFilter.tag = "listener_set"
                    
                    // Clear "All" when clicked/focused
                    binding.actvCategoryFilter.setOnClickListener {
                        if (binding.actvCategoryFilter.text.toString() == "All") {
                            binding.actvCategoryFilter.setText("")
                        }
                        // Show dropdown with all options
                        binding.actvCategoryFilter.post {
                            binding.actvCategoryFilter.showDropDown()
                        }
                    }
                    
                    binding.actvCategoryFilter.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            if (binding.actvCategoryFilter.text.toString() == "All") {
                                binding.actvCategoryFilter.setText("")
                            }
                            // Show dropdown with all options when focused
                            binding.actvCategoryFilter.post {
                                binding.actvCategoryFilter.showDropDown()
                            }
                        }
                    }
                    
                    binding.actvCategoryFilter.setOnItemClickListener { parent, view, position, id ->
                        android.util.Log.d("BookBuddy", "Category filter clicked, position: $position")
                        isUpdatingFilterProgrammatically = true
                        // Get the selected item from the parent adapter (the actual adapter on the view)
                        // instead of the closure variable to avoid stale adapter references
                        val selectedItem = parent.getItemAtPosition(position) as? String
                        if (selectedItem != null) {
                            selectedCategory = if (selectedItem == "All") null else selectedItem
                            android.util.Log.d("BookBuddy", "Selected category: $selectedCategory")
                            // Dismiss dropdown first, before setting text
                            binding.actvCategoryFilter.dismissDropDown()
                            binding.actvCategoryFilter.setText(selectedItem, false)
                            binding.actvCategoryFilter.clearFocus()
                            // Also hide keyboard if visible
                            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                            imm.hideSoftInputFromWindow(binding.actvCategoryFilter.windowToken, 0)
                            isUpdatingFilterProgrammatically = false
                            applyFiltersAndSort()
                        } else {
                            isUpdatingFilterProgrammatically = false
                        }
                    }
                    
                    // Add TextWatcher - show dropdown as user types
                    binding.actvCategoryFilter.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            if (isUpdatingFilterProgrammatically) {
                                // Don't show dropdown when updating programmatically
                                return
                            }
                            val text = s?.toString()?.trim() ?: ""
                            android.util.Log.d("BookBuddy", "Category text changed: '$text'")
                            
                            // Only show dropdown if field has focus (user is typing)
                            if (binding.actvCategoryFilter.hasFocus()) {
                                binding.actvCategoryFilter.post {
                                    if (text.isNotEmpty() && binding.actvCategoryFilter.adapter != null) {
                                        binding.actvCategoryFilter.showDropDown()
                                    } else if (text.isEmpty()) {
                                        // Show all options when empty
                                        binding.actvCategoryFilter.showDropDown()
                                        selectedCategory = null
                                        applyFiltersAndSort()
                                    }
                                }
                            }
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

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchTitle = s?.toString()?.trim() ?: ""
                android.util.Log.d("BookBuddy", "Search text changed: '$searchTitle'")
                applyFiltersAndSort()
            }
        })
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
        
        // Apply author filter - check all author columns (case-insensitive, supports partial matches)
        if (selectedAuthor != null && selectedAuthor!!.isNotEmpty() && selectedAuthor != "All") {
            filtered = filtered.filter { book ->
                // Check if selected author text matches any of the author columns (case-insensitive, partial match)
                val allAuthors = book.getAllAuthors()
                allAuthors.any { it.contains(selectedAuthor!!, ignoreCase = true) } ||
                book.author1?.contains(selectedAuthor!!, ignoreCase = true) == true ||
                book.author2?.contains(selectedAuthor!!, ignoreCase = true) == true ||
                book.author3?.contains(selectedAuthor!!, ignoreCase = true) == true ||
                book.author4?.contains(selectedAuthor!!, ignoreCase = true) == true ||
                book.author5?.contains(selectedAuthor!!, ignoreCase = true) == true
            }
            android.util.Log.d("BookBuddy", "After author filter ($selectedAuthor): ${filtered.size} books")
        } else {
            android.util.Log.d("BookBuddy", "No author filter applied")
        }
        
        // Apply category filter (case-insensitive, supports partial matches)
        if (selectedCategory != null && selectedCategory!!.isNotEmpty() && selectedCategory != "All") {
            filtered = filtered.filter { 
                it.category.contains(selectedCategory!!, ignoreCase = true)
            }
            android.util.Log.d("BookBuddy", "After category filter ($selectedCategory): ${filtered.size} books")
        } else {
            android.util.Log.d("BookBuddy", "No category filter applied")
        }
        
        // Apply title search filter (case-insensitive, word matching)
        if (searchTitle.isNotEmpty()) {
            val searchWords = searchTitle.trim().lowercase().split("\\s+".toRegex())
            filtered = filtered.filter { book ->
                val bookTitle = book.name.lowercase()
                // Check if all search words exist in the title
                searchWords.all { word ->
                    bookTitle.contains(word)
                }
            }
            android.util.Log.d("BookBuddy", "After title search ($searchTitle): ${filtered.size} books")
        } else {
            android.util.Log.d("BookBuddy", "No title search applied")
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
            
            // Observe categories to update adapter's color map
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.categories.collect { categories ->
                    val colorMap = categories.associate { it.name to it.colorHex }
                    adapter.updateCategoryColors(colorMap)
                    android.util.Log.d("BookBuddy", "Updated category colors: ${colorMap.size} categories")
                }
            }
            
            // Observe books to read
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    android.util.Log.d("BookBuddy", "Starting to collect booksToRead...")
                    viewModel.booksToRead.collect { books ->
                        try {
                            android.util.Log.d("BookBuddy", "Received ${books.size} books")
                            currentBooks = books
                            
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

