package com.advice.array.models

sealed class ParityStatus {

    object Valid : ParityStatus()
    class InProgress(val progress: Float) : ParityStatus()
    object Invalid : ParityStatus()
    
}