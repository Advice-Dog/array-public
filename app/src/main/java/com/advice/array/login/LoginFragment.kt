package com.advice.array.login

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import com.advice.array.MainActivity
import com.advice.array.api.response.LoginResponse
import com.advice.array.databinding.LoginFragmentBinding
import com.advice.array.utils.Storage
import com.advice.array.utils.openSupportEmail
import com.advice.array.worker.NotificationWorker
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.HttpException


class LoginFragment : Fragment(), KoinComponent {

    private val storage by inject<Storage>()
    private val analytics by inject<FirebaseAnalytics>()

    private val viewModel by viewModel<LoginViewModel>()

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelNotificationsWorker()

        val address = storage.address
        if (address != null) {
            binding.address.text = address
        }

        val username = storage.username
        if (username != null) {
            binding.username.text = username
        }

        binding.address.setOnTextChangeListener { text ->
            binding.address.error = null
            validate()
        }

        binding.username.setOnTextChangeListener { text ->
            binding.username.error = null
            validate()
        }

        binding.password.setOnTextChangeListener { text ->
            binding.password.error = null
            validate()
        }

        binding.login.setOnClickListener {
            hideKeyboard(requireActivity())

            // Quick fix for copy-paste issues with My Servers URL
            val ipAddress = binding.address.text
                .replace(".unraid.net/login", ".unraid.net")

            val username = binding.username.text
            val password = binding.password.text
            viewModel.login(ipAddress, username, password)
        }

        binding.login.setOnLongClickListener {
            copyUserToClipboard()
            true
        }

        viewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                is LoginScreenState.Init -> {
                    binding.progress.visibility = View.GONE
                    binding.login.isEnabled = false
                }

                is LoginScreenState.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.login.isEnabled = false
                }

                is LoginScreenState.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.login.isEnabled = true
                    val message = when (it.ex) {
                        is HttpException -> {
                            it.ex.message()
                        }

                        else -> it.ex.message
                    }
                    if (message != null) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }

                is LoginScreenState.Success -> {
                    showDashboard(it.data)
                }
            }
        }

        viewModel.helpMessage.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow) {
                AlertDialog.Builder(requireContext())
                    .setCancelable(false)
                    .setTitle("Need help?")
                    .setMessage(
                        "It appears you're having issues logging into your Unraid Server. \nHere are some tips to solve common issues:\n\n" +
                                "- You must use your root account - this will be the same credentials as the WebUI.\n" +
                                "- You must have a password set on your root account for your Unraid server.\n" +
                                "- Ensure your address is correct - try http or https. Unraid.net URLs are supported and recommended.\n" +
                                "- If you use a VPN, try both on and off your VPN.\n" +
                                "\nIf you're still running into issues, please feel free to contact me directly. - Advice"
                    )
                    .setPositiveButton("OK") { _, _ ->
                        // do nothing
                    }
                    .setNegativeButton("CONTACT DEVELOPER") { _, _ ->
                        requireActivity().openSupportEmail()
                    }
                    .setOnDismissListener {
                        viewModel.reset()
                    }
                    .show()
            }
        }

        viewModel.loginMessage.observe(viewLifecycleOwner) { message ->
            binding.message.isVisible = message.isNotEmpty()
            binding.message.text = message
        }

        binding.address.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.address.isSelected = true
                binding.username.isSelected = false
                binding.password.isSelected = false
            } else {
                validateAddress(showError = true)
            }
        }

        binding.username.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.address.isSelected = false
                binding.username.isSelected = true
                binding.password.isSelected = false
            } else {
                validateUsername(showError = true)
            }
        }

        binding.password.setOnFocusChangeListener { _, b ->
            if (b) {
                binding.address.isSelected = false
                binding.username.isSelected = false
                binding.password.isSelected = true
            } else {
                validatePassword(showError = true)
            }
        }


        trackScreen()
    }

    private fun trackScreen() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "login")
        bundle.putString(
            FirebaseAnalytics.Param.SCREEN_CLASS,
            LoginFragment::class.simpleName
        )
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun cancelNotificationsWorker() {
        val instance = WorkManager.getInstance(requireContext())
        instance.cancelAllWorkByTag(NotificationWorker.NOTIFICATION_WORK_TAG)
    }

    private fun validateAddress(showError: Boolean): Boolean {
        val text = binding.address.text

        val isNotBlank = text.isNotBlank()
        val containsWhitespace = text.contains(" ")
        val hasPrefix = text.startsWith("http://") || text.startsWith("https://")

        if (isNotBlank && !hasPrefix && showError) {
            binding.address.error = "Address must start with http:// or https://"
        }

        if (isNotBlank && containsWhitespace) {
            binding.address.error = "Address cannot contain whitespace character"
        }

        return hasPrefix && !containsWhitespace
    }

    private fun validateUsername(showError: Boolean): Boolean {
        val isValid = binding.username.text.isNotBlank()
        if (!isValid && showError) {
            binding.username.error = "Username must not be empty"
        }

        return isValid
    }

    private fun validatePassword(showError: Boolean): Boolean {
        return true
    }


    private fun validate() {
        val isTestUser = binding.username.text == "test" && binding.username.text == "test"
        val isValid =
            validateAddress(showError = false) && validateUsername(showError = false) && validatePassword(
                showError = false
            )
        binding.login.isEnabled = isTestUser || isValid
    }

    private fun showDashboard(data: LoginResponse) {
        (requireActivity() as MainActivity).onLogin(data)
    }

    private fun copyUserToClipboard() {
        val uuid = viewModel.uuid.value
        val clipboard: ClipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("UserID", uuid)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied user id!", Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
