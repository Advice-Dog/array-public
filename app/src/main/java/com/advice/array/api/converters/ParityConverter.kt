package com.advice.array.api.converters

import com.advice.array.models.ParityData
import com.advice.array.utils.parseDateString
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ParityConverter : UnraidConverter<ParityData?>(), KoinComponent {

    private val firebaseCrashlytics by inject<FirebaseCrashlytics>()

    override fun convert(baseUri: String?, string: String): ParityData? {
        if (string.isBlank())
            return null

        return getParityData(baseUri, string)
    }

    private var previousValue: String? = null

    private val formatter = SimpleDateFormat("EEE dd MMM yyyy hh:mm:ss a zzz", Locale.ENGLISH)

    fun getParityData(baseUri: String?, string: String): ParityData {
        previousValue = string

        try {
            val document = Jsoup.parse(string, baseUri, Parser.htmlParser())
            val title = document.head().text()
            if (title.endsWith("/Login")) {
                error("invalid credentials")
            }

            val element = document.childNode(0).childNode(1)

            try {
                val node = element.childNode(0).childNode(0)
                if (node is TextNode && node.text() == "Parity disks not present")
                    return ParityData.NoDiskPresent
            } catch (ex: Exception) {
                // ignore the exception
            }

            // 6.10-rc1 fix
            if (element.childNode(1) is TextNode) {
                element.childNode(0).remove()
            }

            val text = (element.childNode(0) as TextNode).text()

            if (text == "Activity started on " || text == "Current operation started on ") {
                return parseStartedCheck(element)
            }
            if (text == "Last check incomplete on ") {
                return parseIncompleteCheck(element)
            }

            val lastCheck =
                formatter.parse((element.childNode(1).childNode(0) as TextNode).text())

            // Fix for 6.9.0
            val childNode = element.childNode(3)

            val lastErrors = if (childNode.childNodeSize() == 0) {
                // Fix for 6.10.0-rc4
                ""
            } else if (childNode is TextNode && childNode.text().contains("Finding")) {
                (element.childNode(4).childNode(0) as TextNode).text()
            } else {
                (childNode.childNode(0) as TextNode).text()
            }

            val (lastDuration, averageSpeed) = try {
                val temp = (element.childNode(7) as TextNode).text().split(":")
                temp[1].replace(
                    ". Average speed",
                    ""
                ) to temp[2].replace("Next check scheduled on ", "")
            } catch (ex: Exception) {
                "" to ""
            }
            val nextCheck = try {
                try {
                    // fix for 6.10.0-rc4
                    formatter.parse((element.childNode(10).childNode(0) as TextNode).text())
                } catch (ex: Exception) {
                    formatter.parse((element.childNode(8).childNode(0) as TextNode).text())
                }
            } catch (ex: Exception) {
                null
            }

            return ParityData.Valid(
                lastCheck,
                lastErrors,
                lastDuration,
                averageSpeed,
                nextCheck
            )
        } catch (ex: Exception) {
            Timber.e(ex, "Error parsing parity data")
            firebaseCrashlytics.log("Error parsing parity data")
            firebaseCrashlytics.recordException(ex)
            return ParityData.Default(string)
        }
    }

    fun parseStartedCheck(element: Node): ParityData {
        val startDate = formatter.parse((element.childNode(1).childNode(0) as TextNode).text())

        // Fix for 6.9.0
        val childNodes = element.childNodes()

        val currentErrors = try {
            when {
                childNodes[10] is TextNode && (childNodes[10] as TextNode).text()
                    .contains("Finding") -> {
                    (element.childNode(11).childNode(0) as TextNode).text()
                }

                childNodes[3] is TextNode && (childNodes[3] as TextNode).text()
                    .contains("Finding") -> {
                    (element.childNode(4).childNode(0) as TextNode).text()
                }

                else -> {
                    (childNodes[3].childNode(0) as TextNode).text()
                }
            }
        } catch (ex: Exception) {
            firebaseCrashlytics.log("Error parsing current errors")
            ""
        }

        val currentDuration = try {
            when {
                element.childNode(4) is TextNode && (element.childNode(4) as TextNode).text()
                    .contains("Elapsed") -> {
                    (element.childNode(4) as TextNode).text().replace("Elapsed time: ", "")
                }

                element.childNode(6) is TextNode && (element.childNode(6) as TextNode).text()
                    .contains("Elapsed") -> {
                    (element.childNode(6) as TextNode).text().replace("Elapsed time: ", "")
                }

                else -> {
                    (element.childNode(8) as TextNode).text()
                }
            }
        } catch (ex: Exception) {
            firebaseCrashlytics.log("Error parsing current duration")
            ""
        }

        val estimatedFinish = try {
            when {
                element.childNode(7) is TextNode -> {
                    (element.childNode(7) as TextNode).text()
                        .replace("Estimated finish:", "")
                        .parseDateString()
                }

                element.childNode(7).childNodeSize() > 0 -> {
                    (element.childNode(7).childNode(1) as TextNode).text()
                        .replace("Estimated finish:", "")
                        .parseDateString()
                }

                else -> {
                    (element.childNode(11) as TextNode).text()
                        .replace("Estimated finish:", "")
                        .parseDateString()
                }
            }
        } catch (ex: Exception) {
            firebaseCrashlytics.log("Error parsing estimated finish")
            0L
        }

        val currentDate = Date().time
        val completedDate = Date(currentDate + estimatedFinish)

        val progress =
            (((currentDate - startDate.time).toFloat() / (completedDate.time - startDate.time).toFloat()) * 100).toInt()

        return ParityData.Checking(
            startDate,
            currentErrors,
            currentDuration,
            progress,
            completedDate
        )
    }

    private fun parseIncompleteCheck(element: Node): ParityData {
        val startDate = formatter.parse((element.childNode(1).childNode(0) as TextNode).text())
        val currentErrors = (element.childNode(4).childNode(0) as TextNode).text()
        val reason = (element.childNode(8).childNode(0) as TextNode).text()

        val nextCheck =
            formatter.parse(((element.childNode(10) as Element).childNode(0) as TextNode).text())

        return ParityData.Incomplete(
            startDate,
            currentErrors,
            reason,
            nextCheck
        )
    }

    override fun getValue(): String? {
        return previousValue
    }

}