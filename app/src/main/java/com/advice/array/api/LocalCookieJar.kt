package com.advice.array.api

import com.advice.array.utils.Storage
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import timber.log.Timber

class LocalCookieJar(private val storage: Storage) : CookieJar {

    private var cookies = ArrayList<Cookie>()

    init {
        val cache = storage.cookies
        FirebaseCrashlytics.getInstance().log("Creating LocalCookieJar")
        if (cache != null) {
            FirebaseCrashlytics.getInstance().log("Restoring cookies from cache")
            cookies = cache
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        Timber.e(cookies.toString())
        this.cookies.clear()
        this.cookies.addAll(cookies)

        storage.cookies = this.cookies
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        if (url.toString().endsWith("/login", ignoreCase = true))
            return mutableListOf()
        return cookies
    }

    fun getSocketCookies() = cookies.joinToString(separator = "; ") { it.toString() }

    fun setCookies(cookies: List<Cookie>) {
        FirebaseCrashlytics.getInstance().log("Setting cookies")
        this.cookies.clear()
        this.cookies.addAll(cookies)
    }
}