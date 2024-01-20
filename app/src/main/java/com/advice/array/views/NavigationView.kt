package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.advice.array.R
import com.advice.array.databinding.NavigationViewBinding

class NavigationView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: NavigationViewBinding =
        NavigationViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.NavigationView, 0, 0).apply {
            try {
                binding.label.text = getString(R.styleable.NavigationView_navigationLabel)
            } finally {
                recycle()
            }
        }
    }
}