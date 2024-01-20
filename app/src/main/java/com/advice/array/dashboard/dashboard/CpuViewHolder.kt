package com.advice.array.dashboard.dashboard

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.databinding.CpuViewHolderBinding
import com.advice.array.models.Usage

class CpuViewHolder(private val binding: CpuViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(usage: Usage) {
        binding.label.text = "CPU " + usage.type.toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.progress.setProgress(usage.amount, true)
        } else {
            binding.progress.progress = usage.amount
        }
        binding.value.text = "${usage.amount}%"
    }

    companion object {
        fun inflate(parent: ViewGroup): CpuViewHolder {
            val binding =
                CpuViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CpuViewHolder(binding)
        }
    }
}