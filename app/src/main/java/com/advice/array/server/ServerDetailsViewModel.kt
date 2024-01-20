package com.advice.array.server

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.array.api.UnraidRepository
import com.advice.array.api.response.LoginResponse
import com.advice.array.api.response.Response
import com.advice.array.models.DashboardResponse
import com.advice.array.utils.Storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

class ServerDetailsViewModel : ViewModel(), KoinComponent {

    private val repository: UnraidRepository by inject()
    private val storage: Storage by inject()

    val server: LiveData<LoginResponse?>
        get() = _server

    val dashboard: LiveData<DashboardResponse?>
        get() = _dashboard

    val reboot: LiveData<Boolean?>
        get() = _reboot

    private val _server = MutableLiveData<LoginResponse?>()
    private val _dashboard = MutableLiveData<DashboardResponse?>()

    private val _reboot = MutableLiveData<Boolean?>()

    init {
        _server.value = storage.server
    }

    suspend fun login() {
        val address = storage.address
        val (username, password) = storage.username to storage.password
        if (address != null && username != null) {
            when (val result = repository.login(address, username, password)) {
                is Response.Success -> {
                    storage.server = result.data
                    _server.postValue(result.data)
                }
                is Response.Error -> {
                    storage.server = null
                }
            }
        }
    }

    fun reboot(): LiveData<Boolean> {
        val response = MutableLiveData<Boolean>()

        // loading
        response.postValue(null)

        viewModelScope.launch {
            when (val result = repository.rebootSystem()) {
                is Response.Success -> {
                    _reboot.postValue(true)
                    response.postValue(result.data)

                    while (response.hasActiveObservers()) {
                        // Keep logging in.
                        login()

                        // Logged back in, break out.
                        if (_server.value != null) {
                            _reboot.postValue(null)
                            break
                        }
                        delay(5_000)
                    }
                }
                is Response.Error -> {
                    response.postValue(false)
                }
            }
        }

        return response
    }

    fun getRebootTimer(rebooting: Boolean): LiveData<Long> {
        val result = MutableLiveData<Long>()

        val start = Date().time

        viewModelScope.launch {
            // delay for the label and to get an observer.
            delay(5_000)
            while (result.hasActiveObservers()) {
                val milliseconds = Date().time - start
                val time = if (rebooting) {
                    milliseconds
                } else {
                    10_000 - milliseconds
                }
                result.postValue(time)
                delay(500)
            }
        }

        return result
    }

    fun shutdown() {
        viewModelScope.launch {
            when (val result = repository.shutdownSystem()) {
                is Response.Success -> {
                    _reboot.postValue(false)
                }
                is Response.Error -> {

                }
            }
        }
    }
}