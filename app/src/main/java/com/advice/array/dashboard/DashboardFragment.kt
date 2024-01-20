package com.advice.array.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.work.*
import com.advice.array.MainActivity
import com.advice.array.R
import com.advice.array.api.response.LoginResponse
import com.advice.array.dashboard.array.ArrayDashboardFragment
import com.advice.array.dashboard.dashboard.DetailsDashboardFragment
import com.advice.array.dashboard.docker.ContainerFragment
import com.advice.array.dashboard.server.ServerFragment
import com.advice.array.dashboard.shares.SharesFragment
import com.advice.array.databinding.DashboardFragmentBinding
import com.advice.array.notification.NotificationHelper
import com.advice.array.worker.NotificationWorker
import com.github.stkent.amplify.tracking.Amplify
import java.util.concurrent.TimeUnit


class DashboardFragment : Fragment() {

    private var _binding: DashboardFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val obj = arguments?.getParcelable<LoginResponse>("login_response")
            ?: error("login_response cannot be null.")

        val factory = DashboardViewModelFactory(obj)
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        if (savedInstanceState == null) {
            setPromptView()
        }

        val isDeepLink = requireActivity().intent.getBooleanExtra(
            NotificationHelper.NOTIFICATION_DEEP_LINK,
            false
        )
        if (isDeepLink) {
            requireActivity().intent = Intent()
            (requireActivity() as MainActivity).openNotifications()
        }

        setServer()
        setNavigation()
        setNotifications()

        setupNotificationWorker()
    }

    private fun setPromptView() {
        Amplify.getSharedInstance().promptIfReady(binding.promptView)
    }

    private fun setNavigation() {
        binding.pager.isUserInputEnabled = false
        binding.pager.offscreenPageLimit = 1

        val adapter = ViewPagerAdapter(childFragmentManager, requireActivity().lifecycle)
        adapter.addFragment(DetailsDashboardFragment.newInstance(isDashboard = false))
        adapter.addFragment(ArrayDashboardFragment.newInstance(isDashboard = false))
        adapter.addFragment(SharesFragment.newInstance(isDashboard = false))
        adapter.addFragment(ContainerFragment.newInstance(isDashboard = false))
        adapter.addFragment(ServerFragment.newInstance(isDashboard = false))

        binding.pager.adapter = adapter

        binding.bottomBar.setOnNavigationItemSelectedListener {
            onNavigationItemSelected(it)
            true
        }
    }

    private fun setupNotificationWorker() {
        val instance = WorkManager.getInstance(requireContext())
        //instance.cancelAllWorkByTag(NotificationWorker.NOTIFICATION_WORK_TAG)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .addTag(NotificationWorker.NOTIFICATION_WORK_TAG)
            .build()

        instance.enqueueUniquePeriodicWork(
            NotificationWorker.NOTIFICATION_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun onNavigationItemSelected(item: MenuItem) {
        val fragment = when (item.itemId) {
            R.id.dashboard -> 0
            R.id.disks -> 1
            R.id.shares -> 2
            R.id.containers -> 3
            R.id.server -> 4
            else -> null
        }

        if (fragment != null) {
            binding.pager.setCurrentItem(fragment, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("key", "1")
        super.onSaveInstanceState(outState)
    }

    private fun setServer() {
        viewModel.getServer()
        viewModel.server.observe(viewLifecycleOwner) {
            if (it != null) {
                val server = binding.bottomBar.menu.findItem(R.id.server)
                server.title = it.name
            }
        }
    }

    private fun setNotifications() {
        // Post any notifications
        viewModel.notifications.observe(viewLifecycleOwner) {
            it.forEach {
                NotificationHelper.postNotification(requireContext(), it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        private val list = ArrayList<Fragment>()
        fun addFragment(fragment: Fragment) = list.add(fragment)
        override fun createFragment(position: Int) = list[position]
        override fun getItemCount() = list.size
    }

    companion object {
        fun newInstance(response: LoginResponse?): DashboardFragment {
            val fragment = DashboardFragment()
            val bundle = Bundle()
            bundle.putParcelable("login_response", response)
            fragment.arguments = bundle
            return fragment
        }
    }
}