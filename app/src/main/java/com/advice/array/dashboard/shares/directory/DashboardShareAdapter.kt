package com.advice.array.dashboard.shares.directory

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.models.DashboardShare

class DashboardShareAdapter : ListAdapter<DashboardShare, DashboardShareViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardShareViewHolder {
        return DashboardShareViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: DashboardShareViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DashboardShare>() {
            override fun areItemsTheSame(
                oldItem: DashboardShare,
                newItem: DashboardShare
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: DashboardShare,
                newItem: DashboardShare
            ): Boolean {
                return oldItem.description == newItem.description
                        && oldItem.security == newItem.security
                        && oldItem.streams == newItem.streams
            }
        }
    }
}