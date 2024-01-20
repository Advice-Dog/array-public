package com.advice.array.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.advice.array.R
import com.advice.array.databinding.SmallCircluarProgressViewBinding

class SmallCircularProgressView(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    private val binding: SmallCircluarProgressViewBinding =
        SmallCircluarProgressViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SmallCircularProgressView, 0, 0)
            .apply {
                try {
                    val showLabel = getInt(R.styleable.SmallCircularProgressView_small_label, 1)
                    binding.value.visibility = if (showLabel == 1) View.VISIBLE else View.GONE
                } finally {
                    recycle()
                }
            }
    }

    var progress: Int
        get() = binding.progress.progress
        set(value) {
            if (value == -1) {
                binding.root.visibility = View.GONE
            } else {
                binding.root.visibility = View.VISIBLE
                binding.value.text = "$value%"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.progress.setProgress(value, true)
                } else {
                    binding.progress.progress = value
                }
            }
        }
}