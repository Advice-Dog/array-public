package com.advice.array.api.converters

import com.advice.array.api.config.ConfigManager
import com.advice.array.models.Device
import com.advice.array.utils.VersionParser
import com.advice.array.utils.toBytes
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class DeviceConverter : UnraidConverter<List<Device>>(), KoinComponent {

    private val configManager: ConfigManager by inject()
    private val firebaseCrashlytics: FirebaseCrashlytics by inject()

    private var previousValue: String? = null

    override fun convert(baseUri: String?, string: String): List<Device> {
        previousValue = string
        if (string.isEmpty()) {
            firebaseCrashlytics.recordException(IllegalStateException("Null value passed in"))
            return emptyList()
        }

        val document = Jsoup.parse("<table>${string}</table>", baseUri)
        val rows = document.select("tr")

        // No device row
        if (rows.isEmpty() || rows.size == 1 && rows.first().text() == "No devices") {
            return emptyList()
        }

        val filter = rows.filter { it.children().size > 0 }
        val list = filter.mapNotNull { it.toDetailedDevice(configManager.version) }
            .filter { !it.device.contains("Spin Up") }

        if (list.isEmpty()) {
            firebaseCrashlytics.recordException(IllegalStateException("List is empty"))
        }

        return list
    }

    private fun Element.toDetailedDevice(version: String): Device? {
        try {
            val children = children()

            // array not started
            if (children.size == 4) {
                if (VersionParser.isGreaterOrEqual(version, "6.12.0", false)) {
                    try {
                        val device = (children[0].childNode(2) as Element).text()
                        val status =
                            (children[0].childNode(1).childNode(1).childNode(0) as TextNode).text()
                                .split(" ")
                                .first()

                        val identifier = (children[1].childNode(1) as Element).text()

                        return Device(
                            status,
                            device,
                            identifier,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            -1,
                            -1,
                            -1,
                            -1
                        )
                    } catch (ex: Exception) {
                        Timber.e("Could not convert Element to Device (6.12.0): ${ex.message}")
                        firebaseCrashlytics.log("Could not convert Element to Device (6.12.0): ${ex.message}")
                        firebaseCrashlytics.recordException(ex)
                        return null
                    }
                }

                val device = children[0].child(1).text()
                val status = children[0].child(0).text().split(" ").first()
                val identifier =
                    (children[1].childNode(0).childNode(2).childNode(0)
                        .childNode(0) as TextNode).text()
                val temp = (children[2].childNode(0) as TextNode).text()

                return Device(
                    status,
                    device,
                    identifier,
                    temp,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    -1,
                    -1,
                    -1,
                    -1
                )
            }

            // device not present
            if (children.size <= 3) {
                firebaseCrashlytics.log("Child size less than or equal 3")
                return null
            }

            if (VersionParser.isGreaterOrEqual(version, "6.12.0", true)) {
                // Ignoring any summary information rows
                if (childNode(0).childNode(0).attributes().get("class") == "info") {
                    return null
                }

                val device = (children[0].childNode(2) as Element).text()
                val status =
                    (children[0].childNode(1).childNode(1).childNode(0) as TextNode).text()
                        .split(" ")
                        .first()
                val identifier = (children[1].childNode(1) as Element).text()
                val temp = children[2].text()

                val readSpeed = children[3].child(0).text()
                val reads = children[3].child(1).text()

                val writeSpeed = children[4].child(0).text()
                val writes = children[4].child(1).text()

                val errors = children[5].text()

                val fileSystem = children[6].text()
                val size = if (children.size > 7) children[7].text() else ""
                val used = if (children.size > 8) children[8].text() else ""
                val free = if (children.size > 9) children[9].text() else ""

                val usage = if (used != "" && size != "") {
                    ((used.toBytes() / size.toBytes().toFloat()) * 100f).toInt()
                } else {
                    -1
                }

                return Device(
                    status,
                    device,
                    identifier,
                    temp,
                    readSpeed,
                    reads,
                    writeSpeed,
                    writes,
                    errors,
                    fileSystem,
                    size.toBytes(),
                    used.toBytes(),
                    free.toBytes(),
                    usage
                )
            }

            val device = try {
                children[0].child(2).text()
            } catch (ex: Exception) {
                children[0].child(1).text()
            }
            val status = children[0].child(0).text().split(" ").first()
            val identifier = try {
                children[1].child(1).text()
            } catch (ex: Exception) {
                null
            }
            val temp = children[2].text()

            val readSpeed = children[3].child(0).text()
            val reads = children[3].child(1).text()

            val writeSpeed = children[4].child(0).text()
            val writes = children[4].child(1).text()

            val errors = children[5].text()

            val fileSystem = children[6].text()
            val size = if (children.size > 7) children[7].text() else ""
            val used = if (children.size > 8) children[8].text() else ""
            val free = if (children.size > 9) children[9].text() else ""

            val usage = if (used != "" && size != "") {
                ((used.toBytes() / size.toBytes().toFloat()) * 100f).toInt()
            } else {
                -1
            }

            return Device(
                status,
                device,
                identifier,
                temp,
                readSpeed,
                reads,
                writeSpeed,
                writes,
                errors,
                fileSystem,
                size.toBytes(),
                used.toBytes(),
                free.toBytes(),
                usage
            )
        } catch (ex: Exception) {
            Timber.e(ex, "Could not convert Element to Device")
            firebaseCrashlytics.log("Failed to parse Device")
            firebaseCrashlytics.recordException(ex)
            return null
        }
    }

    override fun getValue(): String? {
        return previousValue
    }
}