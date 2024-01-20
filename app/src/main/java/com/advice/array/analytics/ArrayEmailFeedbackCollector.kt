package com.advice.array.analytics

import android.content.Context
import com.advice.array.utils.Storage
import com.github.stkent.amplify.*
import com.github.stkent.amplify.feedback.DefaultEmailFeedbackCollector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

class ArrayEmailFeedbackCollector(recipients: String) : DefaultEmailFeedbackCollector(recipients),
    KoinComponent {

    private val storage by inject<Storage>()

    override fun getBody(app: IApp, environment: IEnvironment, device: IDevice): String {
        val androidVersionString =
            String.format("%s (%s)", environment.androidVersionName, environment.androidVersionCode)
        val appVersionString = String.format("%s (%s)", app.versionName, app.versionCode)

        val uuid = storage.uuid
        val unraidVersion = storage.config?.version ?: "N/A"
        val language = Locale.getDefault().language

        return """
             Time Stamp: ${getCurrentUtcTimeStringForDate(Date())}
             App Version: $appVersionString
             Unraid Version: $unraidVersion
             User: $uuid
             Install Source: ${app.installSource}
             Android Version: $androidVersionString
             Device Manufacturer: ${device.manufacturer}
             Device Model: ${device.model}
             Display Resolution: ${device.resolution}
             Device Language: $language
             ---------------------""".trimIndent()
    }

    private fun getCurrentUtcTimeStringForDate(date: Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z", Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(date)
    }

    fun getEmailBody(applicationContext: Context): String {
        return getBody(
            App(applicationContext),
            Environment(applicationContext),
            Device(applicationContext)
        )
    }
}