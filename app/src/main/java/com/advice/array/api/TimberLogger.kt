package com.advice.array.api

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

class TimberLogger : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Timber.d(message)
    }
}