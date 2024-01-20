package com.advice.array.models

import android.os.Parcelable
import com.advice.array.utils.VersionParser
import kotlinx.android.parcel.Parcelize
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode


@Parcelize
data class VirtualMachine(
    val id: String,
    val name: String,
    val icon: String,
    val state: String,
    val description: String,
    val cpus: String,
    val memory: String,
    val vdisks: String,
    val graphics: String,
    val autostart: String
) : Parcelable

fun VirtualMachine.setAddress(address: String) = VirtualMachine(
    id,
    name,
    address + icon,
    state,
    description,
    cpus,
    memory,
    vdisks,
    graphics,
    autostart
)

fun createMockVirtualMachine() =
    VirtualMachine(
        "1", "windows 10", "", "started", "windows virtual machine",
        "1", "64 GB", "", "", "true"
    )