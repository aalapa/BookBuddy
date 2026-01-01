package com.bookbuddy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bookbuddy.R
import com.bookbuddy.data.Book
import com.bookbuddy.data.BookStatus
import com.bookbuddy.databinding.FragmentAddEditBookBinding
import com.bookbuddy.ui.viewmodel.BookViewModel
import com.bookbuddy.ui.viewmodel.BookViewModelFactory
import kotlinx.coroutines.launch
import java.util.Date

class AddEditBookFragment : Fragment() {
    private var _binding: FragmentAddEditBookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookViewModel by viewModels {
        BookViewModelFactory(requireActivity().application)
    }
    private var isEditMode = false
    private var currentBook: Book? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get bookId from arguments
        val bookId = arguments?.getLong("bookId", -1L) ?: -1L
        isEditMode = bookId != -1L

        if (isEditMode) {
            loadBook(bookId)
        } else {
            // Auto-fill ranking for new books
            setupAutoRanking()
        }

        setupClickListeners()
        setupCategoryInput()
        setupTitleCaseConversion()
    }
    
    private fun setupTitleCaseConversion() {
        // Apply title case when user finishes typing in book name
        binding.etBookName.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus && view is android.widget.EditText) {
                val text = view.text.toString().trim()
                if (text.isNotEmpty()) {
                    val titleCased = toTitleCase(text)
                    if (text != titleCased) {
                        view.setText(titleCased)
                        view.setSelection(titleCased.length) // Move cursor to end
                    }
                }
            }
        }
        
        // Apply title case when user finishes typing in author
        binding.etAuthor.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus && view is android.widget.EditText) {
                val text = view.text.toString().trim()
                if (text.isNotEmpty()) {
                    // For authors, we need to handle multiple authors separated by ; or |
                    val authors = text.split(Regex("[;|]"))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .map { toTitleCase(it) }
                    
                    val titleCased = authors.joinToString(if (text.contains(";")) "; " else " | ")
                    if (text != titleCased) {
                        view.setText(titleCased)
                        view.setSelection(titleCased.length) // Move cursor to end
                    }
                }
            }
        }
        
        // Note: Category title case is handled in setupCategoryInput() to avoid conflicts
    }

    private fun setupAutoRanking() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get current count of books to read
            var count = 0
            viewModel.booksInQueueCount.collect { queueCount ->
                count = queueCount
                if (!isEditMode && _binding != null) {
                    val nextRanking = count + 1
                    binding.etRanking.setText(nextRanking.toString())
                }
            }
        }
    }

    private fun loadBook(bookId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val book = viewModel.getBookById(bookId)
                currentBook = book
                if (_binding != null) {
                    book?.let {
                        binding.etBookName.setText(it.name)
                        // Display all authors when editing (use getAllAuthors() helper or fallback to author field)
                        val authorDisplay = if (it.getAllAuthors().isNotEmpty()) {
                            it.getAllAuthors().joinToString("; ")
                        } else {
                            it.author
                        }
                        binding.etAuthor.setText(authorDisplay)
                        binding.etCategory.setText(it.category)
                        binding.etRanking.setText(it.ranking.toString())
                        if (it.hasBook) {
                            binding.rgHasBook.check(R.id.rbYes)
                        } else {
                            binding.rgHasBook.check(R.id.rbNo)
                        }

                        if (it.startDate != null) {
                            binding.tvStartDate.visibility = View.VISIBLE
                            binding.tvStartDate.text = "Start Date: ${formatDate(it.startDate)}"
                        }

                        if (it.endDate != null) {
                            binding.tvEndDate.visibility = View.VISIBLE
                            binding.tvEndDate.text = "End Date: ${formatDate(it.endDate)}"
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Error loading book", e)
            }
        }
    }

    private fun formatDate(date: Date?): String {
        return if (date != null) {
            val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            format.format(date)
        } else {
            ""
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveBook()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCategoryInput() {
        val categoryInput = binding.etCategory as? AutoCompleteTextView
        if (categoryInput == null) return

        // Populate categories dropdown
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                val categoryNames = categories.map { it.name }.sorted()
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
                categoryInput.setAdapter(adapter)
                
                // Allow adding new category
                categoryInput.setOnItemClickListener { _, _, position, _ ->
                    val selected = categoryNames[position]
                    categoryInput.setText(selected, false)
                }
                
                // Add new category when focus is lost and text is not empty
                // Also apply title case conversion
                categoryInput.setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus && view is android.widget.EditText) {
                        val text = view.text.toString().trim()
                        if (text.isNotEmpty()) {
                            // Apply title case
                            val titleCased = toTitleCase(text)
                            if (text != titleCased) {
                                view.setText(titleCased)
                                view.setSelection(titleCased.length)
                            }
                            
                            // Add category if it doesn't exist
                            val categoryName = titleCased
                            if (categoryName.isNotEmpty() && !categoryNames.contains(categoryName)) {
                                viewModel.addCategory(categoryName)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts a string to title case (capitalizes first letter of each word)
     */
    private fun toTitleCase(text: String): String {
        if (text.isEmpty()) return text
        
        return text.split(" ")
            .joinToString(" ") { word ->
                if (word.isEmpty()) {
                    word
                } else {
                    // Capitalize first letter, lowercase the rest
                    word.first().uppercaseChar() + word.drop(1).lowercase()
                }
            }
    }

    private fun parseAuthors(authorInput: String): Pair<String, List<String>> {
        // Parse authors separated by ; or |
        val authors = authorInput.split(Regex("[;|]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { toTitleCase(it) } // Convert each author to title case
        
        // Return display string (comma-separated) and list of individual authors
        val displayAuthor = authors.joinToString(", ")
        val authorList = authors.take(5) // Limit to 5 authors
        
        return Pair(displayAuthor, authorList)
    }

    private fun saveBook() {
        val bookNameRaw = binding.etBookName.text.toString().trim()
        val authorInputRaw = binding.etAuthor.text.toString().trim()
        val categoryRaw = binding.etCategory.text.toString().trim()
        val rankingText = binding.etRanking.text.toString().trim()
        val hasBook = binding.rgHasBook.checkedRadioButtonId == R.id.rbYes

        // Convert to title case
        val bookName = toTitleCase(bookNameRaw)
        val category = toTitleCase(categoryRaw)

        if (bookName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter book name", Toast.LENGTH_SHORT).show()
            return
        }

        if (authorInputRaw.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter author name", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter category", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse multiple authors (title case conversion happens inside parseAuthors)
        val (displayAuthor, authorList) = parseAuthors(authorInputRaw)
        
        // Ensure category is added to the database (already in title case)
        viewModel.addCategory(category)

        val ranking = try {
            rankingText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Please enter a valid ranking", Toast.LENGTH_SHORT).show()
            return
        }

        val book = if (isEditMode) {
            val existing = currentBook
            if (existing != null) {
                existing.copy(
                    name = bookName,
                    author = displayAuthor, // Display string for backward compatibility
                    author1 = authorList.getOrNull(0),
                    author2 = authorList.getOrNull(1),
                    author3 = authorList.getOrNull(2),
                    author4 = authorList.getOrNull(3),
                    author5 = authorList.getOrNull(4),
                    category = category,
                    ranking = ranking,
                    hasBook = hasBook,
                    createdAt = existing.createdAt, // Preserve original creation date
                    totalReadingDays = existing.totalReadingDays, // Preserve accumulated reading days
                    currentReadingStartDate = existing.currentReadingStartDate // Preserve current session
                )
            } else {
                Toast.makeText(requireContext(), "Error: Book data not loaded", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            Book(
                name = bookName,
                author = displayAuthor, // Display string for backward compatibility
                author1 = authorList.getOrNull(0),
                author2 = authorList.getOrNull(1),
                author3 = authorList.getOrNull(2),
                author4 = authorList.getOrNull(3),
                author5 = authorList.getOrNull(4),
                category = category,
                ranking = ranking,
                hasBook = hasBook,
                status = BookStatus.NOT_STARTED,
                createdAt = Date(), // Set creation date when adding new book
                totalReadingDays = 0, // New book, no reading days yet
                currentReadingStartDate = null // No active reading session
            )
        }

        if (isEditMode) {
            viewModel.updateBook(book)
        } else {
            viewModel.insertBook(book)
        }

        // Navigate back after a short delay to ensure the operation is queued
        view?.postDelayed({
            if (isAdded && _binding != null) {
                findNavController().popBackStack()
            }
        }, 100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

