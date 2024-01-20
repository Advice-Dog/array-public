package com.advice.array.views

import android.content.Context
import android.text.Html
import android.text.SpannableString
import android.text.format.DateUtils.getRelativeTimeSpanString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.advice.array.R
import com.advice.array.databinding.ParityViewBinding
import com.advice.array.models.Parity
import com.advice.array.models.ParityData
import com.advice.array.models.ParityStatus
import java.util.*


class ParityView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val binding: ParityViewBinding =
        ParityViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var _parity: Parity? = null

    var parity: Parity?
        get() = _parity
        set(value) {
            _parity = value
            if (value != null) {
                binding.content.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
                binding.defaultView.visibility = View.GONE

                // Failed parsing, display as WebView.
                if (value.data is ParityData.Default) {
                    binding.content.visibility = View.GONE
                    binding.defaultView.visibility = View.VISIBLE

                    val text = SpannableString(Html.fromHtml(value.data.string))
                    binding.defaultView.setText(text, TextView.BufferType.SPANNABLE)
                    return
                }

                when (value.status) {
                    is ParityStatus.InProgress -> {
                        binding.icon.visibility = View.GONE
                        binding.progress.visibility = View.VISIBLE
                        binding.progress.progress = value.status.progress.toInt()

                        if (value.data is ParityData.Checking) {
                            binding.dueIn.text = getCompleteTimestamp(value.data)
                        }
                        binding.lastCheck.text = "In Progress"
                    }

                    is ParityStatus.Valid -> {
                        binding.icon.visibility = View.VISIBLE
                        binding.icon.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_baseline_check_24
                            )
                        )
                        binding.progress.visibility = View.GONE
                        if (value.data is ParityData.Valid) {
                            binding.dueIn.text = getDueTimestamp(value.data.nextCheck)
                        } else if (value.data is ParityData.Incomplete) {
                            binding.dueIn.text = getDueTimestamp(value.data.nextCheck)
                        }
                        binding.lastCheck.text = "Valid"
                    }

                    is ParityStatus.Invalid -> {
                        binding.icon.visibility = View.VISIBLE
                        binding.icon.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_baseline_close_24
                            )
                        )
                        binding.progress.visibility = View.GONE
                        if (value.data is ParityData.Incomplete) {
                            binding.dueIn.text = getDueTimestamp(value.data.nextCheck)
                            binding.lastCheck.text = value.data.status.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        } else if (value.data is ParityData.Checking) {
                            binding.dueIn.text = getCompleteTimestamp(value.data)
                            binding.lastCheck.text = "Invalid"
                        }
                    }
                }

                // todo: set onClickListener -
                // open bottom sheet with action view layout
                // checked: <date>
                // duration: <duration>
                // average speed: <speed>
                // next: <date>

            } else {
                binding.content.visibility = View.INVISIBLE
                binding.defaultView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        }

    private fun getCompleteTimestamp(value: ParityData.Checking): String {
        val time = value.estimatedComplete?.time ?: return ""
        return getRelativeTimeSpanString(time).toString().replace("In ", "") + " remaining"
    }

    private fun getDueTimestamp(value: Date?): String {
        val time = value?.time ?: return ""
        return getRelativeTimeSpanString(time).toString()
    }
}