package com.advice.array.dashboard.docker.logs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.databinding.LogViewHolderBinding
import org.koin.core.component.KoinComponent


class LogViewHolder(private val binding: LogViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root), KoinComponent {

    fun bind(log: String) {
        binding.log.text = log
    }

    companion object {
        fun inflate(parent: ViewGroup): LogViewHolder {
            val binding =
                LogViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LogViewHolder(binding)
        }
    }
}