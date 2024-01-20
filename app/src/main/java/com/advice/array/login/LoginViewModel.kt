package com.advice.array.login

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.advice.array.api.UnraidRepository
import com.advice.array.api.response.Response
import com.advice.array.models.firebase.FirebaseLoginConfig
import com.advice.array.utils.Storage
import com.advice.array.utils.isHttps
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginViewModel : ViewModel(), KoinComponent {

    private val repository: UnraidRepository by inject()
    private val storage: Storage by inject()
    private val analytics: FirebaseAnalytics by inject()

    val status: LiveData<LoginScreenState>
        get() = _status

    private var shouldShowHelpMessage: Boolean = false

    val helpMessage: LiveData<Boolean>
        get() = _helpMessage

    private val _status = MutableLiveData<LoginScreenState>()
    private val _helpMessage = MutableLiveData(false)
    private var errorCount = 0

    val uuid: LiveData<String>
        get() = _uuid

    private val _uuid = MutableLiveData<String>()

    val loginMessage: LiveData<String>
        get() = _loginMessage

    private val _loginMessage = MutableLiveData<String>()

    init {
        _uuid.value = storage.uuid

        _status.postValue(LoginScreenState.Init)

        val address = storage.address
        val username = storage.username
        val password = storage.password
        if (address != null && username != null) {
            login(address, username, password, track = false)
        }

        fetchLoginConfig()
    }

    fun login(address: String, username: String, password: String?, track: Boolean = true) {
        _status.postValue(LoginScreenState.Loading)
        viewModelScope.launch {
            storage.address = address
            when (val result = repository.login(address, username, password)) {
                is Response.Success -> {
                    errorCount = 0

                    if (track) onLogin(isSuccess = true, address.isHttps())
                    storage.server = result.data
                    storage.username = username
                    storage.password = password
                    _status.postValue(LoginScreenState.Success(result.data))
                }

                is Response.Error -> {
                    if (++errorCount >= 3 && shouldShowHelpMessage) {
                        _helpMessage.postValue(true)
                    }
                    if (track) onLogin(isSuccess = false, address.isHttps())
                    _status.postValue(LoginScreenState.Error(result.exception))
                }
            }
        }
    }

    private fun onLogin(isSuccess: Boolean, isHttps: Boolean) {
        val bundle = Bundle()
        bundle.putBoolean("is_success", isSuccess)
        bundle.putBoolean("is_https", isHttps)
        analytics.logEvent("login", bundle)
    }

    fun reset() {
        _helpMessage.value = false
        errorCount = 0
    }

    private fun fetchLoginConfig() {
        val db = Firebase.firestore
        db.collection("config")
            .document("login")
            .get()
            .addOnSuccessListener {
                try {
                    val config = it.toObject(FirebaseLoginConfig::class.java)
                    _loginMessage.value = config?.message
                    shouldShowHelpMessage = config?.shouldShowHelpMessage ?: false
                } catch (ex: Exception) {
                    // do nothing
                }
            }
    }
}
