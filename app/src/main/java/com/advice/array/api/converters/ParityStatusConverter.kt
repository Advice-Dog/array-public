package com.advice.array.api.converters

import com.advice.array.models.ParityStatus
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ParityStatusConverter : UnraidConverter<ParityStatus?>(), KoinComponent {

    private val firebaseCrashlytics by inject<FirebaseCrashlytics>()

    override fun convert(baseUri: String?, string: String): ParityStatus? {
        return getParityStatus(baseUri, string)
    }

    private var previousValue: String? = null

    fun getParityStatus(baseUri: String?, string: String): ParityStatus? {
        previousValue = string

        if (string.isBlank())
            return null

        val document = Jsoup.parse(string, baseUri, Parser.htmlParser())

        if (document.getElementsByClass("green").isNotEmpty())
            return ParityStatus.Valid

        val orange = document.getElementsByClass("orange")
        if (orange.isNotEmpty()) {
            val text = (orange.first().childNode(0) as TextNode).text()
            val regex = Regex("\\d+.\\d")
            val progress = regex.find(text)?.value?.toFloatOrNull()
            return ParityStatus.InProgress(progress ?: 0.0f)
        }

        return ParityStatus.Invalid
    }

    override fun getValue(): String? {
        return previousValue
    }
}