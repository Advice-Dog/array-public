package com.advice.array.models

data class Notification(
    val timestamp: String,
    val event: String,
    val description: String,
    val file: String,
    val importance: String,
    val subject: String
) {
    val id: Int
        get() = file.hashCode()
}