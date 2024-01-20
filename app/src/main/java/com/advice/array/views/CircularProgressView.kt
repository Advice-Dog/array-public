package com.advice.array.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.advice.array.databinding.CircluarProgressViewBinding

class CircularProgressView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val binding: CircluarProgressViewBinding =
        CircluarProgressViewBinding.inflate(LayoutInflater.from(context), this, true)

    var progress: Int
        get() = binding.progress.progress
        set(value) {
            binding.value.text = "$value%"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.progress.setProgress(value, true)
            } else {
                binding.progress.progress = value
            }
        }
}