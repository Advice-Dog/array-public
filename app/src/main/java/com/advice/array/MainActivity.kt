package com.advice.array

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.advice.array.api.response.LoginResponse
import com.advice.array.dashboard.DashboardFragment
import com.advice.array.dashboard.docker.logs.LogsFragment
import com.advice.array.dashboard.shares.directory.DirectoryFragment
import com.advice.array.databinding.ActivityMainBinding
import com.advice.array.login.LoginFragment
import com.advice.array.models.DockerContainer
import com.advice.array.models.Share
import com.advice.array.notification.NotificationFragment
import com.advice.array.settings.SettingsFragment
import org.koin.core.component.KoinComponent


class MainActivity : AppCompatActivity(), KoinComponent {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null) {
            showLogin()
        }
    }

    private fun showLogin() {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.mainContainer.id, LoginFragment(), "login_fragment")
            .commit()
    }

    fun openDirectory(share: Share) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                binding.mainContainer.id,
                DirectoryFragment.newInstance(share.name),
                "directory_fragment"
            )
            .addToBackStack(null)
            .commit()
    }

    fun openDirectory(directory: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                binding.mainContainer.id,
                DirectoryFragment.newInstance(directory),
                "directory_fragment"
            )
            .addToBackStack(null)
            .commit()
    }

    fun openSettings() {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.mainContainer.id, SettingsFragment.newInstance(), "settings_fragment")
            .addToBackStack(null)
            .commit()
    }

    fun openNotifications() {
        supportFragmentManager
            .beginTransaction()
            .replace(
                binding.mainContainer.id,
                NotificationFragment.newInstance(),
                "notification_fragment"
            )
            .addToBackStack(null)
            .commit()
    }

    fun openLogs(container: DockerContainer) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                binding.mainContainer.id,
                LogsFragment.newInstance(container),
                "logs_fragment"
            )
            .addToBackStack(null)
            .commit()
    }

    fun onLogin(response: LoginResponse? = null) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                binding.mainContainer.id,
                DashboardFragment.newInstance(response),
                "dashboard_fragment"
            )
            .commit()
    }

    fun onLogout() {
        showLogin()
    }
}