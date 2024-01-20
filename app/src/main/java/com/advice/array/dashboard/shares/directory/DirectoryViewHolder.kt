package com.advice.array.dashboard.shares.directory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.R
import com.advice.array.databinding.ShareViewHolderBinding
import com.advice.array.models.Directory
import java.text.SimpleDateFormat

class DirectoryViewHolder(
    private val binding: ShareViewHolderBinding,
    private val onShareClickListener: (Directory) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(directory: Directory) {
        binding.name.text = directory.name
        val formatter = SimpleDateFormat("MMMM d, yyyy")
        binding.description.text = formatter.format(directory.lastModified)

        binding.size.text = when (directory) {
            is Directory.Folder -> null
            is Directory.File -> directory.size
        }

        val drawable = when (directory) {
            is Directory.Folder -> ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.ic_baseline_folder_open_24
            )
            is Directory.File -> ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.ic_outline_insert_drive_file_24
            )
        }

        binding.icon.setImageDrawable(drawable)

        binding.container.setOnClickListener {
            onShareClickListener.invoke(directory)
        }
    }

    companion object {
        fun inflate(
            parent: ViewGroup,
            onShareClickListener: (Directory) -> Unit
        ): DirectoryViewHolder {
            val binding = ShareViewHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DirectoryViewHolder(binding, onShareClickListener)
        }
    }
}