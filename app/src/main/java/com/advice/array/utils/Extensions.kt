package com.advice.array.utils

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.advice.array.ArrayApp
import com.advice.array.analytics.ArrayEmailFeedbackCollector
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.math.roundToLong

private const val BITS_PER_KB = 1024
private const val BITS_PER_MB = 1_048_576
private const val BITS_PER_GB = 1_073_741_824
private const val BITS_PER_TB = 1_099_511_627_776

fun String.toSize() = this.replace(",", "").toLong().toSize()

fun Long.toSize(): String {
    if (this < BITS_PER_KB)
        return "$this B"

    if (this < BITS_PER_MB)
        return String.format("%.1f", this / BITS_PER_KB.toFloat()) + " KB"

    if (this < BITS_PER_GB)
        return String.format("%.1f", this / BITS_PER_MB.toFloat()) + " MB"

    if (this < BITS_PER_TB)
        return String.format("%.1f", this / BITS_PER_GB.toFloat()) + " GB"

    return String.format("%.1f", this / BITS_PER_TB.toFloat()) + " TB"
}

fun Long.toSpeed(): String {
    return toSize() + "PS"
}

fun String.toBytes(): Long {
    if (isBlank())
        return -1

    if (this == "-" || this == "---")
        return -1

    if ("Unmountable" in this)
        return -1

    if ("Formatting" in this)
        return -1

    if("Mounting" in this)
        return -1

    return try {
        val (count, units) = split(" ")
        val amount = count.replace(",", ".").toFloat()

        when (units) {
            "TB", "To" -> (amount * BITS_PER_TB).roundToLong()
            "Tb" -> (amount / 8 * BITS_PER_TB).roundToLong()
            "GB", "Go" -> (amount * BITS_PER_GB).roundToLong()
            "Gb"-> (amount / 8 * BITS_PER_GB).roundToLong()
            "MB", "Mo" -> (amount * BITS_PER_MB).roundToLong()
            "Mb" -> (amount / 8 * BITS_PER_MB).roundToLong()
            "KB" -> (amount * BITS_PER_KB).roundToLong()
            "Kb" -> (amount / 8 * BITS_PER_KB).roundToLong()
            "B" -> amount.roundToLong()
            "b" -> (amount / 8).roundToLong()
            else -> throw IllegalArgumentException("Unknown unit type: $units")
        }
    } catch (ex: Exception) {
        FirebaseCrashlytics.getInstance()
            .recordException(IllegalArgumentException("Unparsable String passed to toBytes: $this"))
        -1
    }
}

private const val MILLISECONDS_PER_SECOND = 1_000L
private const val MILLISECONDS_PER_MINUTE = 60_000L
private const val MILLISECONDS_PER_HOUR = 3_600_000L
private const val MILLISECONDS_PER_DAY = 86_400_000L

// 2 days, 9 hours, 3 minutes
// 6 hours, 1 minute
fun String.parseDateString(): Long {
    return split(",").sumOf {
        val (value, units) = it.trim()
            .replace("\u0000", "")
            .split(" ")

        when (units) {
            "day", "days" -> value.toInt() * MILLISECONDS_PER_DAY
            "hour", "hours" -> value.toInt() * MILLISECONDS_PER_HOUR
            "minute", "minutes" -> value.toInt() * MILLISECONDS_PER_MINUTE
            "second", "seconds" -> value.toInt() * MILLISECONDS_PER_SECOND
            else -> error("unknown units: $units")
        }
    }
}

fun String.isHttps() = startsWith("https://")

fun FragmentActivity.openSupportEmail() {
    val collector = ArrayEmailFeedbackCollector(ArrayApp.SUPPORT_EMAIL)

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")

        putExtra(Intent.EXTRA_EMAIL, arrayOf(ArrayApp.SUPPORT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "[Array] Support Request")
        putExtra(Intent.EXTRA_TEXT, collector.getEmailBody(applicationContext))
    }
    startActivity(Intent.createChooser(intent, "Send support email"))
}