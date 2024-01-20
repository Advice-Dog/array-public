package com.advice.array.reboot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.advice.array.MainActivity
import com.advice.array.R
import com.advice.array.databinding.RebootFragmentBinding
import com.advice.array.server.ServerDetailsViewModel

class RebootFragment : Fragment() {

    private var _binding: RebootFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RebootFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isReboot = arguments?.getBoolean("rebooting") ?: error("rebooting cannot be null")

        binding.label.text = if (isReboot) {
            getString(R.string.rebooting)
        } else {
            getString(R.string.shutting_down)
        }

        val viewModel = ViewModelProvider(requireActivity())[ServerDetailsViewModel::class.java]
        viewModel.getRebootTimer(isReboot).observe(viewLifecycleOwner) {
            if (it <= 0) {
                // finish the activity
                (requireActivity() as MainActivity).onLogout()
            } else {
                binding.timeStamp.text = "${it / 1000}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(rebooting: Boolean): RebootFragment {
            val fragment = RebootFragment()
            fragment.arguments = Bundle().apply {
                putBoolean("rebooting", rebooting)
            }
            return fragment
        }
    }

}