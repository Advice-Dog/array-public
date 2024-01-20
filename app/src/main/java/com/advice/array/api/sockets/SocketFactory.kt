package com.advice.array.api.sockets

import com.advice.array.api.LocalCookieJar
import com.advice.array.api.TrustAllCertificates
import com.advice.array.api.config.ConfigManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

class SocketFactory(
    private val configManager: ConfigManager,
    private val cookieJar: LocalCookieJar,
    private val firebaseCrashlytics: FirebaseCrashlytics
) {

    suspend fun create(path: String, onMessageListener: (String?) -> Unit): WebSocket =
        withContext(Dispatchers.IO) {
            val address = configManager.getAddress() ?: ""
            val url = getSocketUrl(address, path)

            val webSocketFactory = WebSocketFactory().apply {
                this.sslSocketFactory = TrustAllCertificates().getSslSocketFactory()
                sslContext = SSLContext.getInstance("TLS").apply {
                    init(
                        null,
                        arrayOf<TrustManager>(TrustAllCertificates()),
                        SecureRandom()
                    )
                }
                verifyHostname = false
            }

            val socket = webSocketFactory.createSocket(url)

            socket.addHeader("Cookie", cookieJar.getSocketCookies())
            socket.addListener(object : WebSocketAdapter() {
                override fun onTextMessage(websocket: WebSocket?, text: String?) {
                    super.onTextMessage(websocket, text)
                    onMessageListener.invoke(text)
                }
            })

            try {
                Timber.e("Connecting socket: $url")
                socket.connect()
            } catch (ex: Exception) {
                val message = "Could not open socket: $url - ${ex.message}"
                Timber.e(message)
                firebaseCrashlytics.log(message)
                firebaseCrashlytics.recordException(ex)
            }
            return@withContext socket
        }
}

fun getSocketUrl(address: String, path: String): String {
    val domain = address
        .replace("http://", "")
        .replace("https://", "")

    val prefix = if (address.contains("https")) "wss://" else "ws://"

    return prefix + ("$domain/sub/$path").replace("//", "/")
}