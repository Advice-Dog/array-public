package com.advice.array.dashboard.docker

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.advice.array.MainActivity
import com.advice.array.api.LocalCookieJar
import com.advice.array.dashboard.DashboardViewModel
import com.advice.array.databinding.DockerContainerBottomSheetBinding
import com.advice.array.models.DockerContainer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DockerContainerBottomSheet : BottomSheetDialogFragment(), KoinComponent {

    private val localCookieJar by inject<LocalCookieJar>()
    private val analytics by inject<FirebaseAnalytics>()

    private var _binding: DockerContainerBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DockerContainerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = arguments?.getParcelable<DockerContainer>("docker_container")
            ?: error("docker container cannot be null")

        viewModel =
            ViewModelProvider(
                requireParentFragment().requireParentFragment().requireParentFragment()
            )[DashboardViewModel::class.java]

        binding.icon.load(container.icon) {
            addHeader("Cookie", localCookieJar.getSocketCookies())
        }

        viewModel.getDockerContainer(container).observe(viewLifecycleOwner) { container ->
            binding.app.text = container.application
            binding.status.state = container.state
            binding.description.text = container.versionState

            val webVisibility = if (container.webUrl.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.actionWeb.visibility = webVisibility
            binding.webDivider.root.visibility = webVisibility

            if (container.isContainer) {
                when (container.state) {
                    "started" -> {
                        binding.actionStart.visibility = View.GONE
                        binding.actionResume.visibility = View.GONE

                        binding.actionStop.visibility = View.VISIBLE
                        binding.actionPause.visibility = View.VISIBLE
                        binding.actionRestart.visibility = View.VISIBLE
                    }
                    "paused" -> {
                        binding.actionStart.visibility = View.GONE
                        binding.actionResume.visibility = View.VISIBLE

                        binding.actionStop.visibility = View.GONE
                        binding.actionPause.visibility = View.GONE
                        binding.actionRestart.visibility = View.VISIBLE
                    }
                    "stopped" -> {
                        binding.actionStart.visibility = View.VISIBLE
                        binding.actionResume.visibility = View.GONE

                        binding.actionStop.visibility = View.GONE
                        binding.actionPause.visibility = View.GONE
                        binding.actionRestart.visibility = View.GONE
                    }
                }
            } else {
                binding.actionStart.visibility = View.GONE
                binding.actionResume.visibility = View.GONE
                binding.actionStop.visibility = View.GONE
                binding.actionPause.visibility = View.GONE
                binding.actionRestart.visibility = View.GONE
                binding.actionRemove.visibility = View.VISIBLE
            }

            binding.actionUpdate.setOnClickListener {
                viewModel.update(container.application)
                analytics.logEvent("update_docker_container", bundleOf())
            }

            binding.actionStart.setOnClickListener {
                send(container, "start")
                analytics.logEvent("start_docker_container", bundleOf())
            }

            binding.actionResume.setOnClickListener {
                send(container, "resume")
                analytics.logEvent("resume_docker_container", bundleOf())
            }

            binding.actionPause.setOnClickListener {
                send(container, "pause")
                analytics.logEvent("pause_docker_container", bundleOf())
            }

            binding.actionStop.setOnClickListener {
                send(container, "stop")
                analytics.logEvent("stop_docker_container", bundleOf())
            }

            binding.actionRestart.setOnClickListener {
                send(container, "restart")
                analytics.logEvent("restart_docker_container", bundleOf())
            }

            binding.actionRemove.setOnClickListener {
                onRemove(container)
            }

            binding.actionWeb.setOnClickListener {
                openWebUi(container)
            }

            binding.actionLogs.setOnClickListener {
                (requireActivity() as MainActivity).openLogs(container)
            }
        }
    }

    private fun openWebUi(container: DockerContainer) {
        if (container.webUrl.isNullOrBlank()) return

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(container.webUrl))
        startActivity(intent)
    }

    private fun onRemove(container: DockerContainer) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove")
            .setMessage("Are you sure you wish to remove ${container.application}?")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Remove") { _, _ ->
                remove(container)

                if (container.isContainer) {
                    analytics.logEvent("remove_docker_container", bundleOf())
                } else {
                    analytics.logEvent("remove_docker_image", bundleOf())
                }
            }
            .show()
    }

    private fun remove(container: DockerContainer) {
        val action = if (container.isContainer) "remove_container" else "remove_image"
        viewModel.send(container.id, container.application, action)
            .observe(viewLifecycleOwner) {
                onSend(container.application, "remove", it)
            }
    }

    private fun send(container: DockerContainer, action: String) {
        viewModel.send(container.id, container.application, action).observe(viewLifecycleOwner) {
            onSend(container.application, action, it)
        }
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
        fun newInstance(container: DockerContainer): DockerContainerBottomSheet {
            val fragment = DockerContainerBottomSheet()
            fragment.arguments = Bundle().apply {
                putParcelable("docker_container", container)
            }
            return fragment
        }
    }

}