package com.advice.array.models

data class Interface(
    val label: String,
    val mode: String,
    val inboundSpeed: Long,
    val outboundSpeed: Long,
    val receivedPackets: Long,
    val transmittedPackets: Long,
    val receiveCounters: InterfaceErrors,
    val transmitCounters: InterfaceErrors,
)

data class InterfaceErrors(
    val errors: Int,
    val drops: Int,
    val overruns: Int
)

