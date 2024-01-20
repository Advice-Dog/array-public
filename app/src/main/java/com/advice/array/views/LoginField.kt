package com.advice.array.views

import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.advice.array.R
import com.advice.array.databinding.LoginFieldBinding

class LoginField(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: LoginFieldBinding =
        LoginFieldBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.LoginField, 0, 0).apply {
            try {
                binding.label.text = getString(R.styleable.LoginField_fieldLabel)
                binding.field.hint = getString(R.styleable.LoginField_fieldHint)
                binding.field.inputType = when (getInt(R.styleable.LoginField_fieldInputType, 0)) {
                    0 -> InputType.TYPE_CLASS_TEXT
                    1 -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    else -> InputType.TYPE_CLASS_TEXT
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    binding.field.setAutofillHints(when (getInt(R.styleable.LoginField_fieldInputAutofillHint, -1)) {
                        0 -> View.AUTOFILL_HINT_USERNAME
                        1 -> View.AUTOFILL_HINT_PASSWORD
                        else -> null
                    })
                }
            } finally {
                recycle()
            }
        }
    }

    var text: String
        get() = binding.field.text.toString()
        set(value) = binding.field.setText(value)

    var error: String?
        get() = binding.error.text.toString()
        set(value) {
            binding.error.text = value
            binding.error.visibility = if (value != null) View.VISIBLE else View.GONE
        }


    override fun setOnFocusChangeListener(listener: OnFocusChangeListener) {
        binding.field.onFocusChangeListener = listener
    }

    fun setOnTextChangeListener(listener: (String) -> Unit) {
        binding.field.addTextChangedListener {
            listener.invoke(it?.toString() ?: "")
        }
    }
}