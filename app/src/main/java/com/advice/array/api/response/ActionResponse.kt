package com.advice.array.api.response

import com.google.gson.annotations.SerializedName

data class ActionResponse(
    @SerializedName("success")
    val success: String?,
    @SerializedName("error")
    val error: String?
)