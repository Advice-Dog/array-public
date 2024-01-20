package com.advice.array.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.databinding.NotificationViewHolderBinding
import com.advice.array.models.Notification

class NotificationViewHolder(private val binding: NotificationViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        notification: Notification,
        isLastElement: Boolean,
        onDismissListener: (Notification) -> Unit
    ) {
        binding.title.text = notification.event + ":" + notification.timestamp
        binding.description.text = notification.subject + "\n" + notification.description
        binding.divider.isVisible = !isLastElement
        binding.imageView.setOnClickListener {
            onDismissListener.invoke(notification)
        }
    }

    companion object {
        fun inflate(parent: ViewGroup): NotificationViewHolder {
            val binding = NotificationViewHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return NotificationViewHolder(binding)
        }
    }
}