package com.advice.array.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.advice.array.databinding.NotificationFragmentBinding

class NotificationFragment : Fragment() {

    private var _binding: NotificationFragmentBinding? = null
    private val binding: NotificationFragmentBinding
        get() = _binding!!

    private lateinit var viewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NotificationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NotificationAdapter {
            viewModel.dismiss(it)
        }
        binding.list.adapter = adapter

        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        viewModel.getState().observe(viewLifecycleOwner) {
            binding.refresh.isRefreshing = false
            binding.progress.isVisible = it.isLoading
            binding.empty.isVisible = !it.isLoading && it.data.isEmpty()
            adapter.submitList(it.data)
        }

        binding.refresh.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.clearNotifications(requireContext())
    }

    companion object {
        fun newInstance() = NotificationFragment()
    }
}