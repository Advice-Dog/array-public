package com.advice.array.dashboard.vm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.advice.array.dashboard.BaseFragment
import com.advice.array.databinding.DashboardVmBinding
import com.google.firebase.analytics.FirebaseAnalytics

class VirtualMachinesFragment : BaseFragment() {

    private var _binding: DashboardVmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardVmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isDashboard = arguments?.getBoolean("is_dashboard") ?: true
        if (!isDashboard) {
            binding.header.visibility = View.GONE
        }

        val adapter = VirtualMachineAdapter {
            val fragment = VirtualMachineBottomSheet.newInstance(it)
            fragment.show(childFragmentManager, "vm_bottom_sheet")
        }

        binding.vms.adapter = adapter
        viewModel.vms.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.empty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }

        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "virtual machines")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            VirtualMachinesFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(isDashboard: Boolean): VirtualMachinesFragment {
            val fragment = VirtualMachinesFragment()
            val bundle = Bundle()
            bundle.putBoolean("is_dashboard", isDashboard)
            fragment.arguments = bundle
            return fragment
        }
    }
}