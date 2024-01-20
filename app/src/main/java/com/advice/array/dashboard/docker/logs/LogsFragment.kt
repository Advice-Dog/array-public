package com.advice.array.dashboard.docker.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.advice.array.databinding.LogsFragmentBinding
import com.advice.array.models.DockerContainer

class LogsFragment : Fragment() {

    private var _binding: LogsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LogsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = arguments?.getParcelable<DockerContainer>("container")
            ?: error("Container cannot be null.")

        val adapter = LogAdapter()

        val viewModel = LogsViewModel(container)

        viewModel.logs.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }

    companion object {
        fun newInstance(container: DockerContainer): LogsFragment {
            val fragment = LogsFragment()
            val bundle = Bundle()
            bundle.putParcelable("container", container)
            fragment.arguments = bundle
            return fragment
        }
    }
}