package com.advice.array.api.converters

import com.advice.array.api.config.ConfigManager
import com.advice.array.models.*
import com.advice.array.utils.VersionParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class DockerContainerConverter : UnraidConverter<List<DockerContainer>>(), KoinComponent {

    private val configManager: ConfigManager by inject()
    private val firebaseCrashlytics: FirebaseCrashlytics by inject()

    override fun convert(baseUri: String?, string: String): List<DockerContainer> {
        // 'var docker' is the start of non-html section
        var varsStart = string.indexOf("var docker")
        if(varsStart == -1)
            varsStart = string.length - 1

        // context moved to onclick listener in 6.10.0-rc4
        if (VersionParser.isGreaterOrEqual(configManager.version, "6.10.0-rc4", false)) {
            val html = string.substring(0, varsStart)

            val document = Jsoup.parse("<table>$html</table>", baseUri)
            val rows = document.select("tr.sortable")
            if (rows.size == 1 && rows.first().text() == "No Docker Containers")
                return emptyList()

            val containers = rows.mapNotNull { element ->
                element.toDockerContainer(configManager.version)
            }
            // orphan images
            val images = document.select("tr.advanced").mapNotNull { it.toImage() }

            return containers + images
        }

        val html = string.substring(0, varsStart)

        // finding all addDockerContainerContext(...) Strings and extracting to DockerContainerContext
        val context = Regex("addDockerContainerContext\\((.*?)\\)").findAll(string)
            .asSequence()
            .toList()
            .map {
                it.toDockerContainerContext()
            }

        val vars = string.substring(varsStart).split(";")

        val document = Jsoup.parse("<table>$html</table>", baseUri)
        val rows = document.select("tr.sortable")
        if (rows.size == 1 && rows.first().text() == "No Docker Containers")
            return emptyList()

        val containers = rows.mapIndexedNotNull { index, element ->
            val webUrl = context.getOrNull(index)?.webUrl
            element.toDockerContainer(configManager.version)?.setWebUrl(webUrl)
        }
        // orphan images
        val images = document.select("tr.advanced").mapNotNull { it.toImage() }

        return containers + images
    }

    private fun Element.toDockerContainer(version: String): DockerContainer? {
        return try {
            val children = this.children()

            if (VersionParser.isGreaterOrEqual(version, "6.12.0", false)) {
                val app = children[0]
                val id = app.child(1).child(0).id()
                val icon = app.child(1).child(0).child(0).attr("src")
                val appName = app.child(1).child(1).child(0).text()

                // handling other languages for state
                val state = try {
                    (app.child(1).child(1).child(3).parent().childNode(2) as Element).className()
                        .split(" ")[2]
                } catch (ex: Exception) {
                    app.child(1).child(1).child(3).text()
                }

                val version = children[1]
                val versionState = version.child(0).text()

                // bridge
                val network = children[2].text()

                val portMappings = children[3].child(0).text().split(" ")
                    .map { it.replace("TCP", "TCP<->").replace("UDP", "UDP<->") }

                val volumeMappings = children[4].text().split(" ")

                val usage = children[5].text().let { it.substring(0, it.indexOf("%") + 1) }

                // uptime and created at in the same String, "Uptime: 34 minutes ago Created: 1 week ago
                val temp = children[7].text().split(" ").filter { ":" !in it && "(" !in it }
                val startIndex = temp.indexOfFirst { it.toIntOrNull() != null }
                val endIndex = if (state == "started") startIndex + 2 else startIndex + 3
                val uptime = try {
                    temp.subList(startIndex, endIndex).joinToString(" ")
                } catch (ex: Exception) {
                    firebaseCrashlytics.log("Failed to get uptime")
                    null
                }

                val created = try {
                    temp.subList(endIndex, temp.size).joinToString(" ")
                } catch (ex: Exception) {
                    firebaseCrashlytics.log("Failed to get created")
                    null
                }

                // context moved to onclick listener 6.10.0-rc4
                val webUrl = try {
                    app.child(1).child(0).attr("onclick").toString()
                        .replace("addDockerContainerContext(", "").replace("'", "").split(",")[7]
                } catch (ex: Exception) {
                    firebaseCrashlytics.log("Failed to get webUrl")
                    null
                }

                return DockerContainer(
                    appName,
                    id,
                    icon,
                    state,
                    versionState,
                    network,
                    portMappings,
                    volumeMappings,
                    usage,
                    uptime,
                    created,
                    webUrl
                )
            }


            val app = children[0]
            val id = app.child(0).child(0).id()
            val icon = app.child(0).child(0).child(0).attr("src")
            val appName = app.child(0).child(1).child(0).text()

            // handling other languages for state
            val state = try {
                (app.child(0).child(1).child(3).parent().childNode(2) as Element).className()
                    .split(" ")[2]
            } catch (ex: Exception) {
                app.child(0).child(1).child(3).text()
            }

            val version = children[1]
            val versionState = version.child(0).text()

            // bridge
            val network = children[2].text()

            val portMappings = children[3].child(0).text().split(" ")
                .map { it.replace("TCP", "TCP<->").replace("UDP", "UDP<->") }

            val volumeMappings = children[4].text().split(" ")

            val usage = children[5].text().let { it.substring(0, it.indexOf("%") + 1) }

            // uptime and created at in the same String, "Uptime: 34 minutes ago Created: 1 week ago
            val temp = children[7].text().split(" ").filter { ":" !in it && "(" !in it }
            val startIndex = temp.indexOfFirst { it.toIntOrNull() != null }
            val endIndex = if (state == "started") startIndex + 2 else startIndex + 3
            val uptime = try {
                temp.subList(startIndex, endIndex).joinToString(" ")
            } catch (ex: Exception) {
                firebaseCrashlytics.log("Could not parse uptime")
                null
            }
            val created = try {
                temp.subList(endIndex, endIndex + 3).joinToString(" ")
            } catch (ex: Exception) {
                firebaseCrashlytics.log("Could not parse created")
                null
            }

            // context moved to onclick listener 6.10.0-rc4
            val webUrl = try {
                app.child(0).child(0).attr("onclick").toString()
                    .replace("addDockerContainerContext(", "").replace("'", "").split(",")[7]
            } catch (ex: Exception) {
                firebaseCrashlytics.log("Could not parse webUrl")
                null
            }

            DockerContainer(
                appName,
                id,
                icon,
                state,
                versionState,
                network,
                portMappings,
                volumeMappings,
                usage,
                uptime,
                created,
                webUrl
            )
        } catch (ex: Exception) {
            Timber.e("Failed to parse Docker Container")
            firebaseCrashlytics.log("Failed to parse Docker Container")
            firebaseCrashlytics.recordException(ex)
            null
        }
    }

    private fun Element.toImage(): DockerContainer? {
        return try {
            val children = this.children()

            val app = (children()[0].childNode(0).childNode(1).childNode(0) as TextNode).text()
                .replace(Regex("[()]"), "")
            val id = children[0].childNode(0).childNode(0).attr("id")
            val icon = children[0].childNode(0).childNode(0).childNode(0).attr("src")
            val state =
                (children[0].childNode(0).childNode(1).childNode(3).childNode(0) as TextNode).text()
            val version = try {
                (children[1].childNode(2) as TextNode).text()
            } catch (ex: Exception) {
                firebaseCrashlytics.log("Could not parse version")
                ""
            }
            val created =
                (children[2].childNode(0) as TextNode).text().split(" ").takeLast(3).joinToString(" ")

            DockerContainer(
                app,
                id,
                icon,
                state,
                version,
                "",
                emptyList(),
                emptyList(),
                "",
                null,
                created,
                isContainer = false
            )
        } catch (ex: Exception) {
            Timber.e("Failed to parse Image")
            firebaseCrashlytics.log("Failed to parse Image")
            firebaseCrashlytics.recordException(ex)
            null
        }
    }
}