package com.advice.array.models

import com.advice.array.dashboard.toTimeStamp
import com.advice.array.utils.VersionParser
import org.jsoup.nodes.Element
import java.util.Locale

data class UPSStatus(
    val status: String,
    val batteryCharge: String,
    val runtimeLeft: String,
    val nominalPower: String,
    val upsLoad: String,
)

fun Element.toUPSStatus(version: String): UPSStatus {
    val children = children()

    if (VersionParser.isGreaterOrEqual(version, "6.11.5", false)) {
        return UPSStatus(
            children[1].text(),
            children[2].text().toPercent(),
            children[3].text().fromTime(),
            children[4].text().lowercase(Locale.getDefault()),
            children[5].text()
        )
    }

    return UPSStatus(
        children[0].text(),
        children[1].text().toPercent(),
        children[2].text().fromTime(),
        children[3].text().lowercase(Locale.getDefault()),
        children[4].text().lowercase(Locale.getDefault()) + " " + children[5].text().toPercent()
    )
}

// 103.1 minutes
private fun String.fromTime(): String {
    return try {
        val seconds = (split(" ").first().toFloat() * 60).toLong()
        return seconds.toTimeStamp()
    } catch (ex: Exception) {
        this
    }
}

private fun String.toPercent(): String {
    return try {
        val value = split(" ").first().toFloat()
        "$value%"
    } catch (ex: Exception) {
        this
    }
}

fun createMockUPSStatus() = UPSStatus(
    "active", "100", "12 hours", "100 KW", "2 W (3%)"
)
