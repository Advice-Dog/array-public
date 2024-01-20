package com.advice.array.dashboard.dashboard.network

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.models.Interface

class InterfaceAdapter : ListAdapter<Interface, InterfaceViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterfaceViewHolder {
        return InterfaceViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: InterfaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Interface>() {
            override fun areItemsTheSame(
                oldItem: Interface,
                newItem: Interface
            ): Boolean {
                return oldItem.label == newItem.label
            }

            override fun areContentsTheSame(
                oldItem: Interface,
                newItem: Interface
            ): Boolean {
                return oldItem.mode == newItem.mode
            }
        }
    }
}