package com.advice.array.api.config

import com.advice.array.utils.Storage
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ConfigManager(
    private val storage: Storage,
    private val firebaseCrashlytics: FirebaseCrashlytics
) {

    private var config: Config

    init {
        val cache = storage.config
        config = cache ?: Config()
    }

    fun getConfig() = config

    val version: String
        get() = config.version

    fun updateConfig(text: String?) {
        config.setConfig(text)
        storage.config = config
        firebaseCrashlytics.log("updating config via String")
        firebaseCrashlytics.log("version is now: " + config.version)
    }

    fun updateConfig(map: Map<String, String>) {
        config.setConfig(map)
        storage.config = config
        firebaseCrashlytics.log("updating config via Map<String, String>")
        firebaseCrashlytics.log("version is now: " + config.version)
    }

    fun getAddress() = storage.address

    fun setAddress(address: String) {
        storage.address = address
    }
}