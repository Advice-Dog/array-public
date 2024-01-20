package com.advice.array.models

import android.os.Parcelable
import com.advice.array.utils.VersionParser
import kotlinx.android.parcel.Parcelize
import org.jsoup.nodes.Element
import timber.log.Timber


@Parcelize
data class Share(
    val protected: Boolean,
    val name: String,
    val comment: String,
    val smb: String,
    val nfs: String,
    val afp: String,
    val cache: String,
    val size: String,
    val free: String
) : Parcelable

fun createMockShare() = Share(false, "data", "user data", "", "", "", "", "8 TB", "2 TB")