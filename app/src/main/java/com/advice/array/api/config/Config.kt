package com.advice.array.api.config

class Config {

    private var map: Map<String, String> = HashMap()

    fun setConfig(text: String?) {
        if (text == null) {
            map = hashMapOf()
            return
        }

        map = text.split("\n")
            .filter { it.isNotBlank() }
            .map {
                val x = it.split(Regex("[=:]")); x[0].replace("\"", "") to x.subList(1, x.size)
                .joinToString(":") { it }.replace("\"", "").trim()
            }
            .toMap()
    }

    fun setConfig(map: Map<String, String>) {
        this.map = map
    }

    val version: String
        get() = map["version"] ?: "0.0.0"

    val csrf: String
        get() {
            if (map.isEmpty()) error("config is empty")
            return map["csrf_token"] ?: error("CSRF Token is null, version: $version")
        }


    val isDeveloper: Boolean
        get() = map["developer"]?.toBoolean() ?: false
}