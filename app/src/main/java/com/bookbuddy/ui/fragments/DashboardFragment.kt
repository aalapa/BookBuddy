package com.bookbuddy.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import com.bookbuddy.databinding.FragmentDashboardBinding
import com.bookbuddy.ui.viewmodel.BookViewModel
import com.bookbuddy.ui.viewmodel.BookViewModelFactory
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookViewModel by viewModels {
        BookViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        // Load statistics when view is created
        viewModel.loadStatistics()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.totalBooksRead.collect { count ->
                    if (_binding != null) {
                        binding.tvTotalBooksRead.text = count.toString()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Error collecting totalBooksRead", e)
            }
        }

        viewModel.booksThisYear.observe(viewLifecycleOwner, Observer { count ->
            if (_binding != null) {
                binding.tvBooksThisYear.text = count.toString()
            }
        })

        viewModel.runRate.observe(viewLifecycleOwner, Observer { rate ->
            if (_binding != null) {
                binding.tvRunRate.text = String.format("%.1f", rate)
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.inProgressBooks.collect { books ->
                    if (_binding != null) {
                        binding.tvCurrentlyReading.text = books.size.toString()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Error collecting inProgressBooks", e)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.booksInQueueCount.collect { count ->
                    if (_binding != null) {
                        binding.tvBooksInQueue.text = count.toString()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BookBuddy", "Error collecting booksInQueueCount", e)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.loadStatistics()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

