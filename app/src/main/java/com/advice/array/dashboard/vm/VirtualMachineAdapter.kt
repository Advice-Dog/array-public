package com.advice.array.dashboard.vm

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.advice.array.models.VirtualMachine

class VirtualMachineAdapter(private val onVMClickListener: (VirtualMachine) -> Unit) :
    ListAdapter<VirtualMachine, VirtualMachineViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VirtualMachineViewHolder {
        return VirtualMachineViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: VirtualMachineViewHolder, position: Int) {
        holder.bind(getItem(position), onVMClickListener)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VirtualMachine>() {
            override fun areItemsTheSame(
                oldItem: VirtualMachine,
                newItem: VirtualMachine
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: VirtualMachine,
                newItem: VirtualMachine
            ): Boolean {
                return oldItem.description == newItem.description
                        && oldItem.state == newItem.state
                        && oldItem.name == newItem.name
            }
        }
    }
}