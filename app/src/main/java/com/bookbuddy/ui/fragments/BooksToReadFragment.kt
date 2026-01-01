package com.bookbuddy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
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
                onDeleteClick = { book -> viewModel.deleteBook(book) },
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
            0
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
                // Not used for drag and drop
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        }
    }

    private var currentBooks: List<Book> = emptyList()

    private fun setupFilterAndSort() {
        // Setup author filter listener
        binding.actvAuthorFilter.setOnItemClickListener { _, _, position, _ ->
            val authors = currentBooks.map { it.author }.distinct().sorted()
            selectedAuthor = if (position == 0) null else authors[position - 1]
            binding.actvAuthorFilter.setText(selectedAuthor ?: "All", false)
            applyFiltersAndSort()
        }

        // Setup category filter listener
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                val categoryNames = categories.map { it.name }.sorted()
                val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("All") + categoryNames)
                binding.actvCategoryFilter.setAdapter(categoryAdapter)
                
                if (binding.actvCategoryFilter.tag == null) {
                    binding.actvCategoryFilter.tag = "listener_set"
                    binding.actvCategoryFilter.setOnItemClickListener { _, _, position, _ ->
                        selectedCategory = if (position == 0) null else categoryNames[position - 1]
                        binding.actvCategoryFilter.setText(selectedCategory ?: "All", false)
                        applyFiltersAndSort()
                    }
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
        var filtered = currentBooks
        
        // Apply author filter
        if (selectedAuthor != null) {
            filtered = filtered.filter { it.author == selectedAuthor }
        }
        
        // Apply category filter
        if (selectedCategory != null) {
            filtered = filtered.filter { it.category == selectedCategory }
        }
        
        // Apply sort
        filtered = when (sortBy) {
            "date_asc" -> filtered.sortedBy { it.createdAt }
            "date_desc" -> filtered.sortedByDescending { it.createdAt }
            else -> filtered.sortedBy { it.ranking } // Default: by ranking
        }
        
        if (_binding != null && ::adapter.isInitialized) {
            adapter.submitList(filtered)
            binding.emptyStateText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

