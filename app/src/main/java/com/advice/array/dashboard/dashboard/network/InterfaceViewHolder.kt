package com.advice.array.dashboard.dashboard.network

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.databinding.InterfaceViewHolderBinding
import com.advice.array.models.Interface

class InterfaceViewHolder(private val binding: InterfaceViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(i: Interface) {
        binding.label.text = i.label
        binding.description.text = i.mode
    }

    companion object {
        fun inflate(parent: ViewGroup): InterfaceViewHolder {
            val binding = InterfaceViewHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return InterfaceViewHolder(binding)
        }
    }
}