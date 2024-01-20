package com.advice.array.dashboard.dashboard

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.models.Usage

class CpuAdapter : ListAdapter<Usage, CpuViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CpuViewHolder {
        return CpuViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: CpuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Usage>() {
            override fun areItemsTheSame(
                oldItem: Usage,
                newItem: Usage
            ): Boolean {
                return oldItem.type == newItem.type
            }

            override fun areContentsTheSame(
                oldItem: Usage,
                newItem: Usage
            ): Boolean {
                return oldItem.amount == newItem.amount
            }
        }
    }
}