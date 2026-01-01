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
                        binding.etAuthor.setText(it.author)
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
                categoryInput.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus && categoryInput.text?.isNotEmpty() == true) {
                        val categoryName = categoryInput.text.toString().trim()
                        if (categoryName.isNotEmpty() && !categoryNames.contains(categoryName)) {
                            viewModel.addCategory(categoryName)
                        }
                    }
                }
            }
        }
    }

    private fun saveBook() {
        val bookName = binding.etBookName.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val category = binding.etCategory.text.toString().trim()
        val rankingText = binding.etRanking.text.toString().trim()
        val hasBook = binding.rgHasBook.checkedRadioButtonId == R.id.rbYes

        if (bookName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter book name", Toast.LENGTH_SHORT).show()
            return
        }

        if (author.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter author name", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter category", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure category is added to the database
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
                    author = author,
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
                author = author,
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

