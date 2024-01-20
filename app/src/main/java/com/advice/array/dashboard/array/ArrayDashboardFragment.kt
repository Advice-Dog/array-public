package com.advice.array.dashboard.array

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import com.advice.array.dashboard.BaseFragment
import com.advice.array.databinding.DashboardArrayBinding
import com.advice.array.models.ParityData
import com.advice.array.models.ParityStatus
import com.google.firebase.analytics.FirebaseAnalytics
import java.text.SimpleDateFormat

class ArrayDashboardFragment : BaseFragment() {

    private var _binding: DashboardArrayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardArrayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDevices()
        setParity()

        viewModel.arrayStatus.observe(viewLifecycleOwner) { isRunning ->
            if (isRunning) {
                binding.startArray.visibility = View.GONE
                binding.stopArray.visibility = View.VISIBLE
                binding.startMover.visibility = View.VISIBLE
            } else {
                binding.startArray.visibility = View.VISIBLE
                binding.stopArray.visibility = View.GONE
                binding.startMover.visibility = View.GONE
            }
        }

        binding.startParityCheck.setOnClickListener {
            viewModel.startParityCheck().observe(viewLifecycleOwner) {
                onAction(it)
            }
            analytics.logEvent("start_parity_check", bundleOf())
        }

        binding.pauseParityCheck.setOnClickListener {
            viewModel.pauseParityCheck().observe(viewLifecycleOwner) {
                onAction(it)
            }
            analytics.logEvent("stop_parity_check", bundleOf())
        }

        binding.startMover.setOnClickListener {
            viewModel.startMover().observe(viewLifecycleOwner) {
                onAction(it)
            }
            analytics.logEvent("start_mover", bundleOf())
        }

        binding.startArray.setOnClickListener {
            viewModel.startArray().observe(viewLifecycleOwner) {
                onAction(it)
            }
            analytics.logEvent("start_array", bundleOf())
        }

        binding.stopArray.setOnClickListener {
            viewModel.stopArray().observe(viewLifecycleOwner) {
                onAction(it)
            }
            analytics.logEvent("stop_array", bundleOf())
        }

        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "array")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            ArrayDashboardFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun onAction(it: Boolean) {
        val message = if (it) "Success" else "Failed"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setParity() {
        viewModel.parity.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.data is ParityData.NoDiskPresent) {
                    binding.parityHeader.visibility = View.GONE
                    binding.parity.visibility = View.GONE
                    binding.parityDivider.root.visibility = View.GONE
                    binding.startParityCheck.visibility = View.GONE
                    binding.pauseParityCheck.visibility = View.GONE
                    return@observe
                }

                binding.parityHeader.visibility = View.VISIBLE
                binding.parity.visibility = View.VISIBLE
                binding.parityDivider.root.visibility = View.VISIBLE

                val formatter = SimpleDateFormat("MMMM d, yyyy")
                binding.parityHeader.subtitle = when (it.data) {
                    is ParityData.NoDiskPresent -> "No Disk Present"
                    is ParityData.Checking -> "Started " + formatter.format(it.data.date)
                    is ParityData.Valid -> "Completed " + formatter.format(it.data.date)
                    is ParityData.Incomplete -> "Last check incomplete on " + formatter.format(it.data.date)
                    is ParityData.Default -> ""
                }

                when (it.status) {
                    is ParityStatus.InProgress -> {
                        binding.startParityCheck.visibility = View.GONE
                        binding.pauseParityCheck.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.startParityCheck.visibility = View.VISIBLE
                        binding.pauseParityCheck.visibility = View.GONE
                    }
                }
            }
            binding.parity.parity = it
        }
    }

    private fun setDevices() {
        viewModel.array.observe(viewLifecycleOwner) {
            binding.array.setView(it)
        }

        viewModel.cache.observe(viewLifecycleOwner) {
            binding.cache.setView(it)
        }

        viewModel.flash.observe(viewLifecycleOwner) {
            binding.flash.setView(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(isDashboard: Boolean): ArrayDashboardFragment {
            val fragment = ArrayDashboardFragment()
            val bundle = Bundle()
            bundle.putBoolean("is_dashboard", isDashboard)
            fragment.arguments = bundle
            return fragment
        }
    }
}