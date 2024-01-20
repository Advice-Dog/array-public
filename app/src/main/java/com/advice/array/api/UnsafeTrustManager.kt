package com.advice.array.api

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class UnsafeTrustManager : X509TrustManager {

    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) = Unit

    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) = Unit

    override fun getAcceptedIssuers() = emptyArray<X509Certificate>()
}

