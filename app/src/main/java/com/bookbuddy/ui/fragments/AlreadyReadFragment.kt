package com.bookbuddy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookbuddy.databinding.FragmentAlreadyReadBinding
import com.bookbuddy.ui.adapters.BookAdapter
import com.bookbuddy.ui.viewmodel.BookViewModel
import com.bookbuddy.ui.viewmodel.BookViewModelFactory
import kotlinx.coroutines.launch

class AlreadyReadFragment : Fragment() {
    private var _binding: FragmentAlreadyReadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookViewModel by viewModels {
        BookViewModelFactory(requireActivity().application)
    }
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlreadyReadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = BookAdapter(
            onEditClick = { },
            onDeleteClick = { },
            onMarkInProgressClick = { },
            onMarkOnHoldClick = { },
            onMarkCompletedClick = { },
            showActionButtons = false
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.completedBooks.collect { books ->
                    if (_binding != null) {
                        adapter.submitList(books)
                        binding.emptyStateText.visibility =
                            if (books.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Error collecting completedBooks", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

