package com.advice.array.dashboard.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.MainActivity
import com.advice.array.dashboard.BaseFragment
import com.advice.array.databinding.DashboardDetailsBinding
import com.advice.array.models.Usage
import com.advice.array.utils.Storage
import com.advice.array.utils.VersionParser
import com.advice.array.utils.toSpeed
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DetailsDashboardFragment : BaseFragment(), KoinComponent {

    private val storage: Storage by inject()

    private var _binding: DashboardDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setServer()
        setCpuUsage()
        setNetworkUsage()
        setMemoryUsage()

        binding.toolbar.setOnMenuItemClickListener {
            (requireActivity() as MainActivity).openNotifications()
            true
        }

        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "dashboard")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            DetailsDashboardFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        if (storage.showCoreList) {
            binding.cpuListContainer.visibility = View.VISIBLE
        } else {
            binding.cpuListContainer.visibility = View.GONE
        }
    }

    private fun setServer() {
        viewModel.server.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.toolbarTitle.text = it.name
            }
        }
    }

    private fun setMemoryUsage() {
        viewModel.memoryUsage.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.memoryHeader.subtitle = "${it[0].amount}%"
                binding.memoryRam.usage = it[0].amount
                binding.memoryFlash.usage = it[1].amount
                binding.memoryLog.usage = it[2].amount
                binding.memoryDocker.usage = it[3].amount
            }
        }
    }

    private fun setCpuUsage() {
        val listAdapter = setCoreList()
        setSparkLine()

        viewModel.cpuLoad.observe(viewLifecycleOwner) {
            if (it != null) {
                val overall = it.first()
                binding.processorHeader.subtitle = overall.toString()
                listAdapter.submitList(it.takeLast(it.size - 1))
            }
        }
    }

    private fun setCoreList(): ListAdapter<Usage, *> {
        val adapter = CpuAdapter()
        binding.cpuList.adapter = adapter
        binding.cpuList.itemAnimator = null
        return adapter
    }

    private fun setSparkLine() {
        val adapter = CpuSparkAdapter()
        binding.sparkview.adapter = adapter

        viewModel.cpuHistory.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun setNetworkUsage() {
        binding.networkSection.isVisible =
            VersionParser.isGreaterOrEqual(viewModel.getServerVersion(), "6.10.0-rc3", false)

        val outboundAdapter = NetworkSparkAdapter()
        binding.networkGraph.adapter = outboundAdapter

        val inboundAdapter = NetworkSparkAdapter()
        binding.inboundGraph.adapter = inboundAdapter

        viewModel.network.observe(viewLifecycleOwner) {
            val outbound = it.sumOf { it.outboundSpeed }
            val inbound = it.sumOf { it.inboundSpeed }
            binding.networkHeader.subtitle = outbound.toSpeed()
            binding.inboundHeader.subtitle = inbound.toSpeed()
        }

        viewModel.networkHistory.observe(viewLifecycleOwner) { history ->
            val inbound = history.map { it.first }
            val outbound = history.map { it.second }
            val max = LongArray(history.size) {
                Math.max(history[it].first, history[it].second)
            }.toList()

            outboundAdapter.submitList(outbound, max = max)
            inboundAdapter.submitList(inbound, max = max)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(isDashboard: Boolean): DetailsDashboardFragment {
            val fragment = DetailsDashboardFragment()
            val bundle = Bundle()
            bundle.putBoolean("is_dashboard", isDashboard)
            fragment.arguments = bundle
            return fragment
        }
    }
}