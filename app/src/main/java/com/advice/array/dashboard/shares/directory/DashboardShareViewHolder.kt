package com.advice.array.dashboard.shares.directory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.databinding.DashboardShareViewHolderBinding
import com.advice.array.models.DashboardShare

class DashboardShareViewHolder(private val binding: DashboardShareViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(i: DashboardShare) {
        binding.name.type = "share"
        binding.name.identifier = i.name
        binding.description.text = i.description
        binding.security.text = i.security
        binding.streams.text = i.streams.toString()
    }

    companion object {
        fun inflate(parent: ViewGroup): DashboardShareViewHolder {
            val binding = DashboardShareViewHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DashboardShareViewHolder(binding)
        }
    }
}