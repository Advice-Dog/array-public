package com.advice.array.api

import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class TrustAllCertificates : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // Trust all client certificates
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // Trust all server certificates
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf() // Return empty array
    }

    fun getSslSocketFactory(): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(this), java.security.SecureRandom())
        return sslContext.socketFactory
    }
}
