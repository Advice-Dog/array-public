package com.advice.array.api.converters

import com.advice.array.models.Interface
import com.advice.array.models.InterfaceErrors
import com.advice.array.utils.toBytes
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class NetworkInterfaceConverter : UnraidConverter<List<Interface>>(), KoinComponent {

    private val firebaseCrashlytics by inject<FirebaseCrashlytics>()

    override fun convert(baseUri: String?, string: String): List<Interface> {
        return getNetworkInterface(string)
    }

    private var previousValue: String? = null

    private fun getNetworkInterface(string: String?): List<Interface> {
        previousValue = string

        if (string == null)
            return emptyList()

        try {
            val list = string.split("\u0001", "\u0000", "<br>")
                .flatMap { if (!it.endsWith("\n")) it.split("\n") else listOf(it) }
                .map { it.replace("\n", "").trim() }
            val count = list.size / 14

            val interfaces = ArrayList<Interface>()

            for (i in 0 until count) {
                val speed = list.subList(i * 5, i * 5 + 5)
                val modePosition = i + count * 5
                val mode = list[modePosition]
                val packetsPositions = i + count * 6
                val packets = list.subList(packetsPositions, packetsPositions + 2)
                val errorsPosition = i + count * 8
                val subList = list.subList(errorsPosition, errorsPosition + 6)
                val errors = subList
                    .map { it.substring(it.indexOf(":") + 1).trim().toInt() }
                    .windowed(3, 3).map {
                        InterfaceErrors(it[0], it[1], it[2])
                    }


                val value = Interface(
                    speed[0],
                    mode.replace("\r", ""),
                    speed[1].replace("ps", "").toBytes(),
                    speed[2].replace("ps", "").toBytes(),
                    packets[0].toLong(),
                    packets[1].toLong(),
                    errors[0],
                    errors[1]
                )

                interfaces.add(value)
            }

            return interfaces
        } catch (ex: Exception) {
            Timber.e(ex)
            firebaseCrashlytics.log("Could not parse network interface.")
            firebaseCrashlytics.recordException(ex)
            return emptyList()
        }
    }

    override fun getValue(): String? {
        return previousValue
    }
}