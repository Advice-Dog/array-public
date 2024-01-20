package com.advice.array.models

import java.util.*

sealed class ParityData(
    val status: String,
    val date: Date?,
    val errors: String,
    val duration: String,
) {
    object NoDiskPresent : ParityData("No Parity Disk", null, "", "")

    class Valid(
        lastCheck: Date?,
        lastErrors: String,
        lastDuration: String,
        val averageSpeed: String,
        val nextCheck: Date?
    ) : ParityData("Valid", lastCheck, lastErrors, lastDuration)

    class Checking(
        startedDate: Date?,
        currentErrors: String,
        currentDuration: String,
        val progress: Int,
        val estimatedComplete: Date?
    ) : ParityData("In Progress", startedDate, currentErrors, currentDuration)

    class Incomplete(
        lastCheck: Date?,
        lastErrors: String,
        reason: String,
        val nextCheck: Date?
    ) : ParityData(reason, lastCheck, lastErrors, "")

    class Default(
        val string: String
    ) : ParityData("Default", null, "", "")
}

