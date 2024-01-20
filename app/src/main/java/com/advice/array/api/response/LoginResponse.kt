package com.advice.array.api.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class LoginResponse(
    val config: HashMap<String, String>,
    val name: String,
    val ip: String,
    val description: String,
    val licenseType: String,
    val startDate: Date
) : Parcelable