package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.advice.array.R
import com.advice.array.databinding.ViewCardHeaderBinding

class CardHeaderView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: ViewCardHeaderBinding =
        ViewCardHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CardHeaderView, 0, 0).apply {
            try {
                binding.title.text = getString(R.styleable.CardHeaderView_titleText)

                val subtitle = getString(R.styleable.CardHeaderView_subtitleText)
                if (subtitle != null) {
                    binding.subtitle.text = subtitle
                }

                val drawable = getDrawable(R.styleable.CardHeaderView_icon)
                if (drawable != null) {
                    binding.icon.setImageDrawable(drawable)
                } else {
                    binding.icon.visibility = View.GONE
                }
            } finally {
                recycle()
            }
        }
    }

    var title: String?
        get() = binding.title.text.toString()
        set(value) {
            binding.title.text = value
        }

    var subtitle: String?
        get() = binding.subtitle.text.toString()
        set(value) {
            binding.subtitle.text = value
            binding.subtitle.visibility = View.VISIBLE
        }
}