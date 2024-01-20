package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.advice.array.R
import com.advice.array.databinding.ActionViewBinding

class ActionView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: ActionViewBinding =
        ActionViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.ActionView, 0, 0).apply {
            try {
                binding.title.text = getString(R.styleable.ActionView_title)
                binding.subtitle.text = getString(R.styleable.ActionView_subtitle)

                val drawable = getDrawable(R.styleable.ActionView_actionIcon)
                binding.icon.setImageDrawable(drawable)
            } finally {
                recycle()
            }
        }
    }
}