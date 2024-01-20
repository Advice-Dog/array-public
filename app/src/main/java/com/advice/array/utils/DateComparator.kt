package com.advice.array.utils

import java.text.SimpleDateFormat

object DateComparator {

    private val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")

    fun isAfter(last: String?, date: String): Boolean {
        if (last == null)
            return true

        return formatter.parse(date)?.after(formatter.parse(last)) ?: false
    }
}