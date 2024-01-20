package com.advice.array.models

data class DashboardResponse(
    val arrayStatus: String,
    val cpu: String,
    val motherboard: String,
    val motherboardTemp: String,
    val memory: String,
    val maxMemSize: String,
    val usageMemSize: String
)

fun createMockDashboardResponse() = DashboardResponse(
    "active", "intel i7 4700K",
    "ASUS Sabertooth 49X", "12", "16 GB", "64 GB", "16 GB"
)