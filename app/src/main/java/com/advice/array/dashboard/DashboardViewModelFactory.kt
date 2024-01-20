package com.advice.array.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.advice.array.api.response.LoginResponse

class DashboardViewModelFactory(private val loginResponse: LoginResponse) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(loginResponse) as T
    }
}