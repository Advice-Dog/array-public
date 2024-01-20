package com.advice.array.dashboard.shares.directory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.advice.array.MainActivity
import com.advice.array.databinding.DirectoryFragmentBinding
import com.advice.array.models.Directory
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.android.inject

class DirectoryFragment : Fragment() {

    private val analytics: FirebaseAnalytics by inject()

    private var _binding: DirectoryFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DirectoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val directory = arguments?.getString("dir") ?: error("share cannot be null")

        val viewModel = ViewModelProvider(this)[ShareViewModel::class.java]
        viewModel.fetch(directory)

        val startIndex = directory.lastIndexOf("/")
        binding.toolbarTitle.text = directory.substring(startIndex + 1)

        val adapter = DirectoryAdapter {
            if (it is Directory.Folder) {
                (requireActivity() as MainActivity).openDirectory(directory + "/" + it.name)
            } else {
                // todo: download the file
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.shares.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.submitList(it)
                binding.empty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.progress.observe(viewLifecycleOwner) {
            binding.progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "directory")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            DirectoryFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(dir: String): DirectoryFragment {
            val fragment = DirectoryFragment()
            fragment.arguments = Bundle().apply {
                putString("dir", dir)
            }
            return fragment
        }
    }
}