package com.advice.array.models

import com.advice.array.utils.VersionParser
import com.advice.array.utils.toBytes
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import timber.log.Timber

data class Device(
    val status: String,
    val device: String,
    val identifier: String?,
    val temp: String,
    val readSpeed: String,
    val reads: String,
    val writeSpeed: String,
    val writes: String,
    val errors: String,
    val fileSystem: String,
    val size: Long,
    val used: Long,
    val free: Long,
    val usage: Int
)

// create a test device for the test server
fun getTestDevice(device: String): Device {
    return Device(
        "active", device, null, "40",
        "", "", "", "", "", "xfs", "8 TB".toBytes(), "2 TB".toBytes(), "6 TB".toBytes(), 30
    )
}