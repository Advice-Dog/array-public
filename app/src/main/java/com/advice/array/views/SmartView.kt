package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.advice.array.R
import com.advice.array.databinding.SmartViewBinding

class SmartView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: SmartViewBinding =
        SmartViewBinding.inflate(LayoutInflater.from(context), this, true)

    var status: String?
        get() = binding.status.text.toString()
        set(value) {
            binding.status.text = value
            binding.icon.setImageDrawable(getStatusDrawable(value))
        }

    private fun getStatusDrawable(status: String?) = when (status) {
        "error" -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_thumb_down_off_alt_24)
        "healthy" -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_thumb_up_off_alt_24)
        else -> ContextCompat.getDrawable(context, R.drawable.ic_baseline_thumb_up_off_alt_24)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        binding.status.visibility = if (w < 250) View.GONE else View.VISIBLE
    }
}