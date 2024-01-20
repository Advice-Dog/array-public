package com.advice.array.models

data class Usage(val type: Int, val amount: Int) {
    override fun toString(): String {
        return "$amount%"
    }
}