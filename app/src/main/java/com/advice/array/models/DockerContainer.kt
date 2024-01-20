package com.advice.array.models

import android.os.Parcelable
import com.advice.array.utils.VersionParser
import kotlinx.android.parcel.Parcelize
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import timber.log.Timber

@Parcelize
data class DockerContainer(
    val application: String,
    val id: String,
    val icon: String?,
    val state: String,
    val versionState: String,
    val network: String,
    val portMappings: List<String>,
    val volumeMappings: List<String>,
    val usage: String,
    val uptime: String?,
    val created: String?,
    val webUrl: String? = null,
    val isContainer: Boolean = true
) : Parcelable



fun DockerContainer.setAddress(address: String) = DockerContainer(
    application,
    id,
    if (icon != null) address + icon else null,
    state,
    versionState,
    network,
    portMappings,
    volumeMappings,
    usage,
    uptime,
    created,
    webUrl,
    isContainer
)

fun DockerContainer.setState(state: String) = DockerContainer(
    application,
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
    webUrl,
    isContainer
)

fun DockerContainer.setWebUrl(webUrl: String?) = DockerContainer(
    application,
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
    webUrl,
    isContainer
)


data class DockerContainerContext(private val list: List<String>) {

    val webUrl: String?
        get() {
            val url = list.getOrNull(7)
            if (url.isNullOrBlank())
                return null

            return url
        }
}

fun MatchResult.toDockerContainerContext(): DockerContainerContext {
    val list = value.replace("addDockerContainerContext(", "")
        .replace("'", "")
        .replace(")", "")
        .split(",")

    return DockerContainerContext(list)
}

fun createMockDockerContainer() = DockerContainer(
    "plex", "1", "", "started", "1.0.0", "bypass", listOf("8080"), listOf("C://data"),
    "", "160 days", "now", null, true
)