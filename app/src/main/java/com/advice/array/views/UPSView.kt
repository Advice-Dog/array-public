package com.advice.array.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.advice.array.databinding.UpsViewBinding
import com.advice.array.models.UPSStatus

class UPSView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val binding: UpsViewBinding =
        UpsViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun render(status: UPSStatus) {
        binding.status.text = status.status
        binding.charge.text = status.batteryCharge
        binding.runtime.text = status.runtimeLeft
        binding.nominal.text = status.nominalPower
    }
}