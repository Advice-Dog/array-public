package com.advice.array.dashboard.array

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.databinding.DeviceViewHolderBinding
import com.advice.array.models.Device

class DeviceViewHolder(private val binding: DeviceViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(device: Device) {
        binding.app.text = device.device
        binding.status.text = device.status
        binding.utilization.progress = device.usage
    }

    companion object {
        fun inflate(parent: ViewGroup): DeviceViewHolder {
            val binding =
                DeviceViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DeviceViewHolder(binding)
        }
    }
}