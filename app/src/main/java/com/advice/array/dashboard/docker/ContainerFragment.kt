package com.advice.array.dashboard.docker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.advice.array.R
import com.advice.array.dashboard.BaseFragment
import com.advice.array.dashboard.vm.VirtualMachinesFragment
import com.advice.array.databinding.DashboardContainersBinding
import com.google.android.material.tabs.TabLayoutMediator

class ContainerFragment : BaseFragment() {

    private var _binding: DashboardContainersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DashboardContainersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ViewPagerAdapter(childFragmentManager, requireActivity().lifecycle)
        adapter.addFragment(DockerContainerFragment.newInstance(isDashboard = false))
        adapter.addFragment(VirtualMachinesFragment.newInstance(isDashboard = false))

        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.icon = getDrawable(position)
            binding.pager.setCurrentItem(tab.position, true)
        }.attach()
    }

    private fun getDrawable(position: Int) = when (position) {
        0 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_docker)
        1 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_computer_24)
        else -> error("could not find icon for position $position")
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        private val list = ArrayList<Fragment>()
        fun addFragment(fragment: Fragment) = list.add(fragment)
        override fun createFragment(position: Int) = list[position]
        override fun getItemCount() = list.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(isDashboard: Boolean): ContainerFragment {
            val fragment = ContainerFragment()
            val bundle = Bundle()
            bundle.putBoolean("is_dashboard", isDashboard)
            fragment.arguments = bundle
            return fragment
        }
    }
}