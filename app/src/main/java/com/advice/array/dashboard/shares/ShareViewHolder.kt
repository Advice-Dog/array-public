package com.advice.array.dashboard.shares

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.advice.array.databinding.ShareViewHolderBinding
import com.advice.array.models.Share

class ShareViewHolder(
    private val binding: ShareViewHolderBinding,
    private val onShareClickListener: (Share) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(share: Share) {
        binding.name.text = share.name
        setNameConstraint(share)
        binding.description.text = share.comment

        binding.container.setOnClickListener {
            onShareClickListener.invoke(share)
        }
    }

    private fun setNameConstraint(share: Share) {
        val set = ConstraintSet()
        set.clone(binding.container)
        if (share.comment.isEmpty()) {
            set.connect(
                binding.name.id,
                ConstraintSet.BOTTOM,
                binding.container.id,
                ConstraintSet.BOTTOM,
                0
            )
        } else {
            set.clear(binding.name.id, ConstraintSet.BOTTOM)
        }

        set.applyTo(binding.container)
    }

    companion object {
        fun inflate(parent: ViewGroup, onShareClickListener: (Share) -> Unit): ShareViewHolder {
            val binding = ShareViewHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ShareViewHolder(binding, onShareClickListener)
        }
    }
}