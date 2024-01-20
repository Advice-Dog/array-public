package com.advice.array.dashboard.vm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.advice.array.api.LocalCookieJar
import com.advice.array.databinding.VirtualMachineViewHolderBinding
import com.advice.array.models.VirtualMachine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VirtualMachineViewHolder(private val binding: VirtualMachineViewHolderBinding) :
    RecyclerView.ViewHolder(binding.root), KoinComponent {

    private val localCookieJar by inject<LocalCookieJar>()

    fun bind(
        vm: VirtualMachine,
        onVMClickListener: (VirtualMachine) -> Unit
    ) {
        binding.app.text = vm.name
        binding.status.state = vm.state
        binding.description.text = vm.description

        binding.icon.load(vm.icon) {
            addHeader("Cookie", localCookieJar.getSocketCookies())
        }

        binding.container.setOnClickListener {
            onVMClickListener.invoke(vm)
        }
    }

    companion object {
        fun inflate(parent: ViewGroup): VirtualMachineViewHolder {
            val binding = VirtualMachineViewHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return VirtualMachineViewHolder(binding)
        }
    }
}