package com.advice.array.api.converters

import com.advice.array.api.config.ConfigManager
import com.advice.array.models.Share
import com.advice.array.utils.VersionParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class ShareConverter : UnraidConverter<List<Share>>(), KoinComponent {

    private val configManager: ConfigManager by inject()
    private val firebaseCrashlytics: FirebaseCrashlytics by inject()

    override fun convert(baseUri: String?, string: String): List<Share> {
        val document = Jsoup.parse("<table>${string}</table>", baseUri)
        val rows = document.select("tr")
        if (rows.size == 1 && rows.first().text() == "No shares")
            return emptyList()

        return rows.mapNotNull { it.toShare(configManager.version) }
    }

    private fun Element.toShare(version: String): Share? {
        try {
            val children = children()

            if (VersionParser.isGreaterOrEqual(version, "6.12.0", false)) {
                val protected = children[0].child(1).text() == "All files protected"
                val name = children[0].child(2).text()
                val comment = children[1].text()
                val smb = ""
                val nfs = ""
                val afp = ""
                val cache = ""
                val size = children[5].text()
                val free = children[6].text()

                return Share(protected, name, comment, smb, nfs, afp, cache, size, free)
            }

            val protected = children[0].child(0).text() == "All files protected"
            val name = children[0].child(1).text()
            val comment = children[1].text()
            val smb = children[2].text()
            val nfs = children[3].text()
            val afp = children[4].text()
            val cache = children[5].text()
            val size = children[6].text()
            val free = children[7].text()

            return Share(protected, name, comment, smb, nfs, afp, cache, size, free)
        } catch (ex: Exception) {
            Timber.e(ex, "Could not convert Element to Share")
            firebaseCrashlytics.log("Could not convert Element to Share: ${ex.message}")
            firebaseCrashlytics.recordException(ex)
            return null
        }
    }
}
