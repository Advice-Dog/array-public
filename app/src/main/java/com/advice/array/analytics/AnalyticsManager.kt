package com.advice.array.analytics

import com.advice.array.api.response.LoginResponse
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager(private val analytics: FirebaseAnalytics) {

    fun setUserProperties(response: LoginResponse) {

        analytics.setUserProperty("license_type", response.licenseType)

        val config = response.config
        analytics.setUserProperty("version", config["version"])
    }

}