package com.advice.array.api.converters

import android.util.Log
import com.advice.array.BuildConfig
import com.advice.array.analytics.LogManager
import com.advice.array.api.response.LoginResponse
import com.advice.array.utils.Storage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.net.URLDecoder
import java.util.*

class LoginResponseConverter : UnraidConverter<LoginResponse?>(), KoinComponent {

    private val logManager: LogManager by inject()
    private val storage: Storage by inject()
    private val gson: Gson by inject()


    override fun convert(baseUri: String?, string: String): LoginResponse {
        try {
            val document = Jsoup.parse(string, baseUri)

            val text = document.head().text()

            if (text.endsWith("/Login")) {
                logManager.log("Could not log the user in.")
                error("invalid credentials")
            }

            val match = Regex("uptime = (\\d+.\\d+)[;]").find(string)
            val uptime = match?.groups?.get(1)?.value?.toDouble() ?: -1.0

            val startDate = Date(System.currentTimeMillis() - (uptime * 1000).toLong())

            // getting config
            val map = try {
                val varsStart = string.indexOf("{\"version")
                val varsEnd = string.indexOf("}", varsStart) + 1
                val subString = string.substring(varsStart, varsEnd)
                gson.fromJson(subString, HashMap::class.java) as HashMap<String, String>
            } catch (ex: Exception) {
                logManager.log("Could not find config, parsing via Regex.")
                // Using regex to find the token.
                val regex = Regex("csrf_token ?= ?(.[A-z\\d]+)|csrf_token:'(.[A-z\\d]+)")
                val matches = regex.findAll(string)
                    .asSequence()
                    .toList()

                if (matches.isEmpty()) {
                    logManager.log("Could not obtain csrf_token")
                    error("Could not obtain csrf_token")
                }

                val token =
                    matches.mapNotNull { it.groups.filterNotNull().last().value.replace("\"", "") }.firstOrNull()
                        ?: error("Could not obtain csrf_token.")

                // Using regex to find the version.
                val versionRegex = Regex("([0-9]\\.[0-9]{1,3}\\.[0-9]{1,3}(-rc[0-9]{1,3})?)")
                val versionMatches = versionRegex.findAll(string)
                    .asSequence()
                    .toList()

                val version = versionMatches.firstOrNull()?.groups?.get(1)?.value ?: "0.0.0"

                logManager.log("Regex returned: csrf_token=$token, version=$version")

                hashMapOf("csrf_token" to token, "version" to version)
            }

            // my servers
            val myServers = document.getElementsByAttribute("apikey")
            if (myServers.isNotEmpty()) {
                logManager.log("LoginResponse contains MyServers element.")
                return getMyServersResponse(map, startDate, myServers[0])
            }

            val (name, ip) = try {
                val nameIp =
                    document.body().child(0).child(1).child(1).child(1).childNodes()[0].toString()
                        .split("•")
                nameIp[0].trim() to nameIp[1].trim()
            } catch (ex: Exception) {
                logManager.log("Could not parse name or ip from LoginResponse.")
                "Tower" to ""
            }

            val description = try {
                document.body().child(0).child(1).child(1).child(1).childNodes()[2].toString()
            } catch (ex: Exception) {
                logManager.log("Could not parse description from LoginResponse.")
                ""
            }
            val basic = try {
                document.body().child(0).child(1).child(1).child(1).getElementById("licensetype")
                    .text()
            } catch (ex: Exception) {
                logManager.log("Could not parse licensetype from LoginResponse.")
                ""
            }

            return LoginResponse(map, name, ip, description, basic, startDate)
        } catch (ex: Exception) {
            // Only log the exception if we have correct credentials
            if (ex.message != "invalid credentials") {
                sendLoginLog(ex.message ?: "unknown error")
            }
            throw ex
        }
    }

    private fun getMyServersResponse(
        config: HashMap<String, String>,
        startDate: Date,
        element: Element
    ): LoginResponse {
        val serverState: LinkedHashMap<String, String>? = Gson().fromJson(
            URLDecoder.decode(element.attr("serverstate")),
            LinkedHashMap::class.java
        ) as? LinkedHashMap<String, String>
        val description = element.attr("serverdesc")

        val name = serverState?.get("servername") ?: ""
        // fix for 6.10-rc1
        val ip = serverState?.get("serverip") ?: serverState?.get("internalip") ?: ""
        val licenseType = serverState?.get("state") ?: ""

        return LoginResponse(
            config,
            name,
            ip,
            description,
            licenseType,
            startDate
        )
    }

    private fun sendLoginLog(error: String) {
        val value = getValue()
        // only report if the csrf_token is there somewhere.
        if (value?.contains("csrf_token =") != true) {
            return
        }

        val db = Firebase.firestore
        val language = Locale.getDefault().language

        val user = hashMapOf(
            "language" to language,
            "version" to (storage.config?.version ?: "0.0.0"),
            "app_version" to BuildConfig.VERSION_NAME
        )

        db.collection("errors")
            .document(storage.uuid)
            .set(user)
            .addOnSuccessListener { ref ->
                Timber.d("DocumentSnapshot added with ID:")
            }.addOnFailureListener { e ->
                Timber.w("Error adding document", e)
            }

        val log = hashMapOf(
            "value" to value,
            "error" to error,
            "language" to language,
            "app_version" to BuildConfig.VERSION_NAME
        )

        val document = this.javaClass.simpleName.lowercase(Locale.getDefault())
            .replace("response", "")
            .replace("converter", "") + "-$language"

        db.collection("errors")
            .document(storage.uuid)
            .collection("logs")
            .document(document)
            .set(log)
            .addOnSuccessListener { ref ->
                Timber.d("DocumentSnapshot added with ID:")
            }.addOnFailureListener { e ->
                Log.w("LoginResponseConverter", "Error adding document", e)
            }
    }
}