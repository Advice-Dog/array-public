package com.advice.array.dashboard

import com.advice.array.models.Device

sealed class DevicesResponse {
    object Loading : DevicesResponse()
    class Success(val devices: List<Device>) : DevicesResponse()
    class Failure(val ex: Exception) : DevicesResponse()
}