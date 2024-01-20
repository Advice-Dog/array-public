package com.advice.array.api.converters

import com.advice.array.api.config.ConfigManager
import com.advice.array.models.Usage
import com.advice.array.utils.VersionParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MemoryUsageConverter : UnraidConverter<List<Usage>?>(), KoinComponent {

    private val configManager by inject<ConfigManager>()
    private val firebaseCrashlytics by inject<FirebaseCrashlytics>()

    override fun convert(baseUri: String?, string: String): List<Usage>? {
        return getMemoryUsage(baseUri, string)
    }

    private var previousValue: String? = null

    private fun getMemoryUsage(baseUri: String?, string: String?): List<Usage>? {
        previousValue = string

        if (string.isNullOrBlank())
            return null

        val document = Jsoup.parse(string, baseUri)

        val text = document.head().text()
        if (text.endsWith("/Login")) {
            firebaseCrashlytics.log("Could not parse memory usage.")
            firebaseCrashlytics.recordException(Exception("Could not parse memory usage."))
        }

        val temp = string
            .replace("\u0000", "")
            .split("%")
            .filter { it.isNotBlank() }

        val list = temp.map { Usage(-1, it.toIntOrNull() ?: 0) }
        if (list.size > 4 && VersionParser.isGreaterOrEqual(configManager.version, "6.12.0", true)) {
            return listOf(list.first()) + list.takeLast(list.size - 2)
        }

        return list
    }

    override fun getValue(): String? {
        return previousValue
    }
}