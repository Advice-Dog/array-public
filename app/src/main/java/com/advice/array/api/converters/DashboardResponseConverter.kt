package com.advice.array.api.converters

import com.advice.array.api.config.ConfigManager
import com.advice.array.models.DashboardResponse
import com.advice.array.utils.VersionParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class DashboardResponseConverter : UnraidConverter<DashboardResponse?>(), KoinComponent {

    private val configManager: ConfigManager by inject()
    private val firebaseCrashlytics: FirebaseCrashlytics by inject()

    override fun convert(baseUri: String?, string: String): DashboardResponse {
        try {
            val document = Jsoup.parse(string, baseUri)

            val text = document.head().text()
            if (text.endsWith("/Login")) {
                error("invalid credentials")
            }


            if (VersionParser.isGreaterOrEqual(
                    configManager.version,
                    "6.12.0",
                    ignoreRevision = true
                )
            ) {
                val sections = document.getElementsByClass("section")

                val cpu = try {
                    (sections[1].parentNode().parentNode().parentNode().childNode(3).childNode(0)
                        .childNode(0) as TextNode).text()
                } catch (ex: Exception) {
                    (sections[1].parentNode().parentNode().parentNode().childNode(1).childNode(0)
                        .childNode(0) as TextNode).text()
                }
                val motherboard = try {
                    (sections[0].parentNode().parentNode().parentNode().childNode(3).childNode(0)
                        .childNode(0) as TextNode).text()
                } catch (ex: Exception) {
                    (sections[0].parentNode().parentNode().parentNode().childNode(1).childNode(0)
                        .childNode(0) as TextNode).text()
                }

                val motherboardTemp = try {
                    (sections[0].childNode(2) as TextNode).text()
                } catch (ex: Exception) {
                    (sections[0].childNode(2) as Element).text()
                }.trim()

                val memory =
                    try {
                        (sections[2].childNode(2).childNode(1) as TextNode).text().trim()
                    } catch (ex: Exception) {
                        ""
                    }
                val maxMemSize = try {
                    try {
                        (sections[2].parentNode().parentNode().parentNode().childNode(3)
                            .childNode(0).childNode(2) as TextNode).text()
                    } catch (ex: Exception) {
                        (sections[2].parentNode().parentNode().parentNode().childNode(1)
                            .childNode(0).childNode(2) as TextNode).text()
                    }
                } catch (ex: Exception) {
                    ""
                }
                val usageMemSize = try {
                    try {
                        (sections[2].parentNode().parentNode().parentNode().childNode(1)
                            .childNode(0).childNode(0).childNode(1) as TextNode).text()
                    } catch (ex: Exception) {
                        (sections[2].parentNode().parentNode().parentNode().childNode(3)
                            .childNode(0).childNode(0).childNode(1) as TextNode).text()
                    }
                } catch (ex: Exception) {
                    ""
                }

                // <span id="statusraid"><span id="statusbar"><span class='green strong'><i class='fa fa-play-circle'></i> Array Started</span></span></span>
                val arrayStatus = (document.getElementById("statusraid").childNode(0).childNode(0)
                    .childNode(1) as TextNode).text().trim()

                return DashboardResponse(
                    arrayStatus,
                    cpu,
                    motherboard,
                    motherboardTemp,
                    memory,
                    maxMemSize,
                    usageMemSize
                )
            }

            val cpu = (document.getElementsByClass("cpu_view").first()
                .childNode(1).childNode(0) as TextNode).text()

            val motherboard = document.getElementsByClass("mb_view").first()
                .childNode(1).childNodes()
                .map { it.toString() }
                .filter { it != "<br>" }
                .joinToString(separator = "\n") { it }
                .trim()

            val motherboardTemp = try {
                (document.getElementsByClass("section")[0].childNode(2) as TextNode).text()
            } catch (ex: Exception) {
                (document.getElementsByClass("section")[0].childNode(2) as Element).text()
            }.trim()

            val memory =
                try {
                    document.getElementsByClass("section")[2].childNode(2).childNode(0).toString()
                        .trim()
                } catch (ex: Exception) {
                    ""
                }
            val maxMemSize =
                try {
                    document.getElementsByClass("mem_view")[0].childNode(1).childNode(0).toString()
                } catch (ex: Exception) {
                    ""
                }
            val usageMemSize =
                try {
                    document.getElementsByClass("mem_view")[1].childNode(1).childNode(0).toString()
                } catch (ex: Exception) {
                    ""
                }

            // <span id="statusraid"><span id="statusbar"><span class='green strong'><i class='fa fa-play-circle'></i> Array Started</span></span></span>
            val arrayStatus = (document.getElementById("statusraid").childNode(0).childNode(0)
                .childNode(1) as TextNode).text().trim()

            return DashboardResponse(
                arrayStatus,
                cpu,
                motherboard,
                motherboardTemp,
                memory,
                maxMemSize,
                usageMemSize
            )
        } catch (ex: Exception) {
            Timber.e("Could not convert DashboardResponse: ${ex.message}")
            firebaseCrashlytics.log("Could not convert DashboardResponse")
            firebaseCrashlytics.recordException(ex)
            throw ex
        }
    }
}