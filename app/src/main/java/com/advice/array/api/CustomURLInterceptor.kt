package com.advice.array.api

import com.advice.array.utils.Storage
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException

class CustomURLInterceptor(private val storage: Storage) : Interceptor {

    companion object {

        const val BASE_URL = "http://tower"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val baseUrl = storage.address ?: return chain.proceed(request)

        val url = request.url()

        val toString = url.toString()
            .replace(BASE_URL, baseUrl)

        try {
            val builder = request.newBuilder()
                .url(toString)

            return chain.proceed(builder.build())
        } catch (ex: Exception) {
            throw IOException(ex.message)
        }
    }
}
