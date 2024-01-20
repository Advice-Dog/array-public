package com.advice.array.api.converters

import com.advice.array.api.config.ConfigManager
import com.advice.array.models.UPSStatus
import com.advice.array.models.toUPSStatus
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class UPSConverter : UnraidConverter<UPSStatus?>(), KoinComponent {

    private val configManager by inject<ConfigManager>()
    private val firebaseCrashlytics by inject<FirebaseCrashlytics>()

    override fun convert(baseUri: String?, string: String): UPSStatus? {
        val document = Jsoup.parse("<table>$string</table>", baseUri)
        val rows = document.select("tr")
        return try {
            rows.first().toUPSStatus(configManager.version)
        } catch (ex: Exception) {
            Timber.e("Could not convert Element to UPSStatus: ${ex.message}")
            firebaseCrashlytics.log("Could not convert Element to UPSStatus: ${ex.message}")
            firebaseCrashlytics.recordException(ex)
            null
        }
    }
}