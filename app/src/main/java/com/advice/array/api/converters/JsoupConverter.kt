package com.advice.array.api.converters

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import retrofit2.Converter
import java.nio.charset.Charset

class JsoupConverter(private val baseUri: String) : Converter<ResponseBody, Document?> {

    override fun convert(value: ResponseBody): Document? {
        val charset = value.contentType()?.charset() ?: Charset.forName("UTF-8")

        val parser = when (value.contentType().toString()) {
            "application/xml", "text/xml" -> Parser.xmlParser()
            else -> Parser.htmlParser()
        }

        val string = value.string()
        if (string.startsWith("<tr")) {
            return Jsoup.parse("<table>$string</table>", baseUri)
        }
        return Jsoup.parse(string, baseUri, parser)
    }
}