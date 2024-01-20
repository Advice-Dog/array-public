package com.advice.array.dashboard.shares

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.advice.array.MainActivity
import com.advice.array.dashboard.BaseFragment
import com.advice.array.dashboard.shares.directory.DashboardShareAdapter
import com.advice.array.databinding.DashboardSharesBinding
import com.google.firebase.analytics.FirebaseAnalytics

class SharesFragment : BaseFragment() {

    private var _binding: DashboardSharesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardSharesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isDashboard = arguments?.getBoolean("is_dashboard") ?: true
        if (!isDashboard) {
            //binding.header.visibility = View.GONE
            showShares()
        } else {
            showDashboardShares()
        }

        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "shares")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            SharesFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun showShares() {
        val adapter = ShareAdapter {
            (requireActivity() as MainActivity).openDirectory(it)
        }

        binding.shares.adapter = adapter
        viewModel.shares.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.empty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDashboardShares() {
        val adapter = DashboardShareAdapter()

        binding.shares.adapter = adapter
        viewModel.dashboardShares.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.empty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(isDashboard: Boolean): SharesFragment {
            val fragment = SharesFragment()
            val bundle = Bundle()
            bundle.putBoolean("is_dashboard", isDashboard)
            fragment.arguments = bundle
            return fragment
        }
    }
}