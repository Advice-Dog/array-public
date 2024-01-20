package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.advice.array.R
import com.advice.array.dashboard.DevicesResponse
import com.advice.array.dashboard.array.DeviceAdapter
import com.advice.array.databinding.DevicesListViewBinding
import com.advice.array.utils.toSize

class DevicesListView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: DevicesListViewBinding =
        DevicesListViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val adapter = DeviceAdapter()

    init {
        binding.list.adapter = adapter

        context.theme.obtainStyledAttributes(attrs, R.styleable.DevicesListView, 0, 0).apply {
            try {
                binding.header.title = getString(R.styleable.DevicesListView_devicesListTitle)

                val subtitle = getString(R.styleable.DevicesListView_devicesListSubtitle)
                if (subtitle != null) {
                    binding.header.subtitle = subtitle
                }
            } finally {
                recycle()
            }
        }
    }

    fun setView(response: DevicesResponse) {
        binding.header.subtitle = getSubtitle(response)

        when (response) {
            DevicesResponse.Loading -> {
                binding.empty.visibility = View.GONE
                binding.list.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
            }
            is DevicesResponse.Failure -> {
                binding.empty.visibility = View.VISIBLE
                binding.empty.text = response.ex.message
                binding.list.visibility = View.GONE
                binding.progress.visibility = View.GONE
            }
            is DevicesResponse.Success -> {
                binding.empty.isVisible = response.devices.isEmpty()
                binding.empty.text = context.getString(R.string.empty_array_message)
                binding.list.isVisible = response.devices.isNotEmpty()
                binding.progress.visibility = View.GONE
                adapter.submitList(response.devices)
            }
        }
    }

    private fun getSubtitle(response: DevicesResponse): String {
        when (response) {
            is DevicesResponse.Failure -> return ""
            DevicesResponse.Loading -> return ""
            is DevicesResponse.Success -> {
                val list = response.devices

                if (list.all { it.used == -1L }) {
                    return "stopped"
                }

                val used = list.sumOf { it.used }
                val total = list.sumOf { it.size }
                val percent = if (total > 0) (used.toFloat() / total * 100f) else 0.0f

                return "${used.toSize()} used of ${total.toSize()} (${
                    String.format(
                        "%.1f",
                        percent
                    )
                }%)"
            }
        }
    }
}