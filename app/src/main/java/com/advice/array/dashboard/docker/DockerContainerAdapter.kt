package com.advice.array.dashboard.docker

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.models.DockerContainer

class DockerContainerAdapter(private val onDockerContainerClickListener: (DockerContainer) -> Unit) :
    ListAdapter<DockerContainer, DockerContainerViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DockerContainerViewHolder {
        return DockerContainerViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: DockerContainerViewHolder, position: Int) {
        holder.bind(getItem(position), onDockerContainerClickListener)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DockerContainer>() {
            override fun areItemsTheSame(
                oldItem: DockerContainer,
                newItem: DockerContainer
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: DockerContainer,
                newItem: DockerContainer
            ): Boolean {
                return oldItem.state == newItem.state && oldItem.versionState == newItem.versionState
            }
        }
    }
}