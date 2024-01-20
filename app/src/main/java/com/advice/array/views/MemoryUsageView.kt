package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.advice.array.R
import com.advice.array.databinding.MemoryUsageViewBinding

class MemoryUsageView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val binding: MemoryUsageViewBinding =
        MemoryUsageViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.MemoryUsageView, 0, 0).apply {
            try {
                val label = getString(R.styleable.MemoryUsageView_memory_label)
                if (label != null) {
                    binding.label.visibility = View.VISIBLE
                    binding.label.text = label
                } else {
                    binding.label.visibility = View.GONE
                }
            } finally {
                recycle()
            }
        }
    }

    var usage: Int
        get() = binding.progress.progress
        set(value) {
            binding.progress.progress = value
        }
}