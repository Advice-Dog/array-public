package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.advice.array.R
import com.advice.array.databinding.DeviceViewBinding

class DeviceView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: DeviceViewBinding =
        DeviceViewBinding.inflate(LayoutInflater.from(context), this, true)

    var identifier: String?
        get() = binding.identifier.text.toString()
        set(value) {
            binding.identifier.text = value
        }

    var type: String?
        get() = binding.icon.tag?.toString()
        set(value) {
            binding.icon.tag = type
            binding.icon.setImageDrawable(getStatusDrawable(value))
        }

    private fun getStatusDrawable(status: String?) = when (status) {
        "device" -> ContextCompat.getDrawable(context, R.drawable.ic_disk)
        "cache" -> ContextCompat.getDrawable(context, R.drawable.ic_nvme)
        "unassigned" -> ContextCompat.getDrawable(context, R.drawable.ic_disk)
        "share" -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_folder_open_24)
        else -> ContextCompat.getDrawable(context, R.drawable.ic_disk)
    }
}