package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.advice.array.R
import com.advice.array.databinding.StatusViewBinding

class StatusView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: StatusViewBinding =
        StatusViewBinding.inflate(LayoutInflater.from(context), this, true)

    var state: String? = null
        set(value) {
            updateState(value)
            field = value
        }

    private fun updateState(state: String?) {
        when (state) {
            "stopped" -> {
                binding.statusIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_round_stop_24
                    )
                )
                val color = ContextCompat.getColor(context, R.color.status_stopped)
                binding.statusIcon.setColorFilter(
                    color,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            "paused" -> {
                binding.statusIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_round_pause_24
                    )
                )
                val color = ContextCompat.getColor(context, R.color.status_paused)
                binding.statusIcon.setColorFilter(
                    color,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            "started" -> {
                binding.statusIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_round_play_arrow_24
                    )
                )
                val color = ContextCompat.getColor(context, R.color.status_active)
                binding.statusIcon.setColorFilter(
                    color,
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            else -> ContextCompat.getColor(context, R.color.text_color)
        }
    }
}