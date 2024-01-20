package com.advice.array.dashboard.vm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.advice.array.api.LocalCookieJar
import com.advice.array.dashboard.DashboardViewModel
import com.advice.array.databinding.VirtualMachineBottomSheetBinding
import com.advice.array.models.VirtualMachine
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VirtualMachineBottomSheet : BottomSheetDialogFragment(), KoinComponent {

    private val localCookieJar by inject<LocalCookieJar>()
    private val analytics by inject<FirebaseAnalytics>()

    private var _binding: VirtualMachineBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VirtualMachineBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = arguments?.getParcelable<VirtualMachine>("virtual_machine")
            ?: error("virtual machine cannot be null")

        viewModel =
            ViewModelProvider(
                requireParentFragment().requireParentFragment().requireParentFragment()
            )[DashboardViewModel::class.java]

        binding.icon.load(container.icon) {
            addHeader("Cookie", localCookieJar.getSocketCookies())
        }

        viewModel.getVirtualMachine(container).observe(viewLifecycleOwner) { container ->
            binding.app.text = container.name
            binding.status.state = container.state
            binding.description.text = container.description

            when (container.state) {
                "started" -> {
                    binding.actionStart.visibility = View.GONE
                    binding.actionResume.visibility = View.GONE

                    binding.actionStop.visibility = View.VISIBLE
                    binding.actionDestroy.visibility = View.VISIBLE
                    binding.actionPause.visibility = View.VISIBLE
                    binding.actionRestart.visibility = View.VISIBLE
                }
                "paused" -> {
                    binding.actionStart.visibility = View.GONE
                    binding.actionResume.visibility = View.VISIBLE

                    binding.actionStop.visibility = View.GONE
                    binding.actionDestroy.visibility = View.VISIBLE
                    binding.actionPause.visibility = View.GONE
                    binding.actionRestart.visibility = View.GONE
                }
                "stopped" -> {
                    binding.actionStart.visibility = View.VISIBLE
                    binding.actionResume.visibility = View.GONE

                    binding.actionStop.visibility = View.GONE
                    binding.actionDestroy.visibility = View.GONE
                    binding.actionPause.visibility = View.GONE
                    binding.actionRestart.visibility = View.GONE
                }
            }

            binding.actionStart.setOnClickListener {
                send(container, "start")
                analytics.logEvent("start_virtual_machine", bundleOf())
            }

            binding.actionResume.setOnClickListener {
                send(container, "resume")
                analytics.logEvent("resume_virtual_machine", bundleOf())
            }

            binding.actionPause.setOnClickListener {
                send(container, "pause")
                analytics.logEvent("pause_virtual_machine", bundleOf())
            }

            binding.actionDestroy.setOnClickListener {
                send(container, "destroy")
                analytics.logEvent("destroy_virtual_machine", bundleOf())
            }

            binding.actionStop.setOnClickListener {
                send(container, "stop")
                analytics.logEvent("stop_virtual_machine", bundleOf())
            }

            binding.actionRestart.setOnClickListener {
                send(container, "restart")
                analytics.logEvent("restart_virtual_machine", bundleOf())
            }
        }
    }

    private fun send(container: VirtualMachine, action: String) {
        viewModel.sendVirtualMachine(
            container.id.replace("vm-", ""),
            container.name,
            "domain-$action"
        ).observe(viewLifecycleOwner, {
            onSend(container.name, action, it)
        })
    }

    private fun onSend(container: String, action: String, success: String?) {
        when (success) {
            null -> {
                binding.progress.visibility = View.VISIBLE
            }
            "true" -> {
                binding.progress.visibility = View.GONE
                val action = (action + "ed").replace("ee", "e")
                Toast.makeText(
                    requireContext(),
                    "Successfully $action $container",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
            else -> {
                binding.progress.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Could not $action $container - $success",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(container: VirtualMachine): VirtualMachineBottomSheet {
            val fragment = VirtualMachineBottomSheet()
            fragment.arguments = Bundle().apply {
                putParcelable("virtual_machine", container)
            }
            return fragment
        }
    }
}