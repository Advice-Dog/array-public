package com.advice.array.dashboard.shares

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.models.Share

class ShareAdapter(private val onShareClickListener: (Share) -> Unit) :
    ListAdapter<Share, ShareViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        return ShareViewHolder.inflate(parent, onShareClickListener)
    }

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Share>() {
            override fun areItemsTheSame(
                oldItem: Share,
                newItem: Share
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: Share,
                newItem: Share
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}