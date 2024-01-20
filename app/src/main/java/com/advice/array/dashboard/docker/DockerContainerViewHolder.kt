package com.advice.array.dashboard.docker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.advice.array.api.LocalCookieJar
import com.advice.array.databinding.DockerContainerViewHolderBinding
import com.advice.array.models.DockerContainer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class DockerContainerViewHolder(private val binding: DockerContainerViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root), KoinComponent {

    private val localCookieJar by inject<LocalCookieJar>()

    fun bind(
        container: DockerContainer,
        onDockerContainerClickListener: (DockerContainer) -> Unit
    ) {
        binding.app.text = container.application
        binding.status.state = container.state
        binding.description.text = container.versionState

        binding.icon.load(container.icon) {
            addHeader("Cookie", localCookieJar.getSocketCookies())
        }

        binding.container.setOnClickListener {
            onDockerContainerClickListener.invoke(container)
        }
    }

    companion object {
        fun inflate(parent: ViewGroup): DockerContainerViewHolder {
            val binding =
                DockerContainerViewHolderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return DockerContainerViewHolder(binding)
        }
    }
}