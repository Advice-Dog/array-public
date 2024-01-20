package com.advice.array.dashboard.server

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import com.advice.array.BuildConfig
import com.advice.array.MainActivity
import com.advice.array.analytics.LogManager
import com.advice.array.analytics.ReportLogsException
import com.advice.array.api.config.ConfigManager
import com.advice.array.dashboard.BaseFragment
import com.advice.array.databinding.ServerFragmentBinding
import com.advice.array.utils.openSupportEmail
import com.advice.array.utils.toSpeed
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class ServerFragment : BaseFragment(), KoinComponent {

    private val logManager by inject<LogManager>()

    private var _binding: ServerFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ServerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDashboard()
        setVersionCode()

        binding.settings.setOnClickListener {
            (requireActivity() as MainActivity).openSettings()
        }

        binding.reboot.setOnClickListener {
            onReboot()
        }

        binding.shutdown.setOnClickListener {
            onShutdown()
        }

        binding.support.setOnClickListener {
            requireActivity().openSupportEmail()
        }

        binding.sendLogs.setOnClickListener {
            showSendLogsDialog()
        }

        binding.logout.setOnClickListener {
            onLogout()
        }

        binding.version.setOnClickListener {
            firebaseCrashlytics.log("version clicked: ${viewModel.uuid.value}")
            firebaseCrashlytics.recordException(ReportLogsException())
            copyUserToClipboard()
        }

        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "server")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            ServerFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun showSendLogsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Send Logs")
            .setMessage("Are you sure you want to send your logs to the developer?")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Send Logs") { _, _ ->
                logManager.sendLogs()
            }
            .show()
    }

    private fun onLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign out")
            .setMessage("Are you sure you wish to sign out?")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Sign out") { _, _ ->
                viewModel.logout()
                (requireActivity() as MainActivity).onLogout()
                analytics.logEvent("logout", bundleOf())
            }
            .show()
    }

    private fun onReboot() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reboot")
            .setMessage("Are you sure you wish to reboot Unraid?")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("reboot") { _, _ ->
                viewModel.reboot().observe(viewLifecycleOwner) {
                    // open the reboot fragment
                }
                analytics.logEvent("reboot", bundleOf())
            }
            .show()
    }

    private fun onShutdown() {
        AlertDialog.Builder(requireContext())
            .setTitle("Shutdown")
            .setMessage("Are you sure you wish to shutdown Unraid?")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Shutdown") { _, _ ->
                viewModel.shutdown()
                analytics.logEvent("shutdown", bundleOf())
            }
            .show()
    }


    private fun setVersionCode() {
        binding.version.text = "Version ${BuildConfig.VERSION_NAME}"
    }

    private fun copyUserToClipboard() {
        val uuid = viewModel.uuid.value
        val clipboard: ClipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("UserID", uuid)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied user id!", Toast.LENGTH_SHORT).show()
    }

    private fun setDashboard() {
        viewModel.server.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.toolbarTitle.text = it.name
                binding.serverHeader.title = "${it.name} • ${it.ip}"
                binding.serverHeader.subtitle = it.description
                binding.server.text =
                    "Unraid OS ${it.licenseType}\n" + "Version " + get<ConfigManager>().getConfig().version
            }
        }

        viewModel.dashboard.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.cpu.text = it.cpu
                binding.motherboardHeader.subtitle = it.motherboardTemp
                binding.motherboard.text = it.motherboard
                binding.memoryHeader.subtitle = it.memory
                binding.memoryMaxSize.text = it.maxMemSize
                binding.memoryUsableSize.text = it.usageMemSize
            }
        }

        viewModel.uptime.observe(viewLifecycleOwner) {
            binding.uptime.text = it
        }

        viewModel.cpuLoad.observe(viewLifecycleOwner) {
            if (it != null) {
                val overall = it.first()
                binding.processorHeader.subtitle = "Overall Load: ${overall.amount}%"
            }
        }

        viewModel.network.observe(viewLifecycleOwner) {
            if (it != null && it.isNotEmpty()) {
                val first = it.first()
                binding.networkHeader.subtitle =
                    "${first.label} Inbound: ${first.inboundSpeed.toSpeed()}, Outbound: ${first.outboundSpeed.toSpeed()}"
            }
        }

        viewModel.ups.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.upsHeader.visibility = View.VISIBLE
                binding.ups.visibility = View.VISIBLE
                binding.upsHeader.subtitle = "UPS Load: ${it.upsLoad}"
                binding.ups.render(it)
            } else {
                binding.upsHeader.visibility = View.GONE
                binding.ups.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        fun newInstance(isDashboard: Boolean): ServerFragment {
            val fragment = ServerFragment()
            val bundle = Bundle()
            bundle.putBoolean("is_dashboard", isDashboard)
            fragment.arguments = bundle
            return fragment
        }
    }
}