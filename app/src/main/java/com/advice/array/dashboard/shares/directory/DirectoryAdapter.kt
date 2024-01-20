package com.advice.array.dashboard.shares.directory

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.models.Directory

class DirectoryAdapter(private val onShareClickListener: (Directory) -> Unit) :
    ListAdapter<Directory, DirectoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryViewHolder {
        return DirectoryViewHolder.inflate(parent, onShareClickListener)
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Directory>() {
            override fun areItemsTheSame(
                oldItem: Directory,
                newItem: Directory
            ): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: Directory,
                newItem: Directory
            ): Boolean {
                return oldItem.lastModified == newItem.lastModified
            }
        }
    }
}