package com.advice.array.api.converters

import com.advice.array.api.config.ConfigManager
import com.advice.array.models.VirtualMachine
import com.advice.array.utils.VersionParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class VirtualMachineConverter : UnraidConverter<List<VirtualMachine>>(), KoinComponent {

    private val configManager by inject<ConfigManager>()
    private val firebaseCrashlytics by inject<FirebaseCrashlytics>()

    override fun convert(baseUri: String?, string: String): List<VirtualMachine> {
        val document = Jsoup.parse("<table>${string}</table>", baseUri)
        val rows = document.select("tr.sortable")
        if (rows.size == 1 && rows.first().text() == "No Virtual Machines installed")
            return emptyList()

        return rows.mapNotNull { it.toVirtualMachine() }
    }

    private fun Element.toVirtualMachine(): VirtualMachine? {
        try {
            val children = children()

            if (VersionParser.isGreaterOrEqual(configManager.version, "6.11.0", false)) {
                val id = children[0].childNode(1).childNode(0).attr("id")
                val name =
                    (children[0].childNode(1).childNode(1).childNode(0)
                        .childNode(0) as TextNode).text()
                val icon = children[0].childNode(1).childNode(0).childNode(0).attr("src")

                val state = (children[0].getElementsByClass("state")[0].parent()
                    .childNode(2) as Element).className()
                    .split(" ")[2]

                val description = children[1].text()
                val cpus = children[2].text()
                val memory = children[3].text()
                val vdisks = children[4].text()
                val graphics = children[5].text()
                val autostart = children[6].text()

                return VirtualMachine(
                    id,
                    name,
                    icon,
                    state,
                    description,
                    cpus,
                    memory,
                    vdisks,
                    graphics,
                    autostart
                )
            }

            val id = children[0].childNode(0).childNode(0).attr("id")
            val name =
                (children[0].childNode(0).childNode(1).childNode(0).childNode(0) as TextNode).text()
            val icon = children[0].childNode(0).childNode(0).childNode(0).attr("src")

            // handling other languages for state
            val state = try {
                (children[0].getElementsByClass("state")[0].parent()
                    .childNode(2) as Element).className()
                    .split(" ")[2]
            } catch (ex: Exception) {
                children[0].getElementsByClass("state")[0].text()
            }

            val description = children[1].text()
            val cpus = children[2].text()
            val memory = children[3].text()
            val vdisks = children[4].text()
            val graphics = children[5].text()
            val autostart = children[6].text()

            return VirtualMachine(
                id,
                name,
                icon,
                state,
                description,
                cpus,
                memory,
                vdisks,
                graphics,
                autostart
            )
        } catch (ex: Exception) {
            Timber.e("Could not convert element to Virtual Machine.", ex)
            firebaseCrashlytics.log("Failed to parse Virtual Machine")
            firebaseCrashlytics.recordException(ex)
            return null
        }
    }
}