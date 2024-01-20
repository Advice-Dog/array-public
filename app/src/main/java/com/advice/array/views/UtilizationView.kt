package com.advice.array.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.advice.array.R
import com.advice.array.databinding.ViewUtilizationBinding

class UtilizationView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val binding: ViewUtilizationBinding =
        ViewUtilizationBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.UtilizationView, 0, 0).apply {
            try {
                val label = getString(R.styleable.UtilizationView_label)
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
            if (value == -1) {
                binding.empty.visibility = View.VISIBLE
                binding.progress.visibility = View.GONE
                binding.label.visibility = View.GONE
            } else {
                binding.empty.visibility = View.GONE
                binding.progress.visibility = View.VISIBLE
                if (binding.label.text.toString() != "")
                    binding.label.visibility = View.VISIBLE


                binding.value.text = "$value%"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.progress.setProgress(value, true)
                } else {
                    binding.progress.progress = value
                }
            }
        }

}