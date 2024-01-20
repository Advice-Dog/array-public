package com.advice.array.dashboard.docker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.advice.array.dashboard.BaseFragment
import com.advice.array.databinding.DockerContainerFragmentBinding
import com.google.firebase.analytics.FirebaseAnalytics

class DockerContainerFragment : BaseFragment() {

    private var _binding: DockerContainerFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DockerContainerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isDashboard = arguments?.getBoolean("is_dashboard") ?: true
        if (!isDashboard) {
            //binding.header.visibility = View.GONE
            binding.containers.layoutManager = LinearLayoutManager(requireContext())
        }

        setDockerContainers()

        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "docker containers")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            DockerContainerFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun setDockerContainers() {
        val adapter = DockerContainerAdapter {
            val fragment = DockerContainerBottomSheet.newInstance(it)
            fragment.show(childFragmentManager, "docker_container_bottom_sheet")
        }

        binding.containers.adapter = adapter
        viewModel.containers.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.empty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(isDashboard: Boolean): DockerContainerFragment {
            val fragment = DockerContainerFragment()
            val bundle = Bundle()
            bundle.putBoolean("is_dashboard", isDashboard)
            fragment.arguments = bundle
            return fragment
        }
    }
}