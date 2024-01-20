package com.advice.array.api.converters

import com.advice.array.models.Directory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class DirectoryConverter : UnraidConverter<List<Directory>>(), KoinComponent {

    private val firebaseCrashlytics by inject<FirebaseCrashlytics>()

    override fun convert(baseUri: String?, string: String): List<Directory> {
        val document = Jsoup.parse("<table>${string}</table>", baseUri)
        val rows = document.select("tr")
        if (rows.size == 0 || rows.size == 2)
            return emptyList()

        return rows.subList(1, rows.size - 1).mapNotNull { it.toDirectory() }
            .sortedWith(compareBy({ it is Directory.File }, { it.name.lowercase(Locale.getDefault()) }))
    }

    private fun Element.toDirectory(): Directory? {
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")

            val children = children()

            val name = children[1].text()
            val size = children[2].text()
            val lastModified = formatter.parse(children[3].text())
            val location = children[4].text()

            if (size.contains("DIR") or size.contains("FOLDER")) {
                return Directory.Folder(name, lastModified, location)
            }

            return Directory.File(name, size, lastModified, location)
        } catch (ex: Exception) {
            Timber.e("Could not convert element to Directory", ex)
            firebaseCrashlytics.log("Failed to parse Directory")
            firebaseCrashlytics.recordException(ex)
            return null
        }
    }
}