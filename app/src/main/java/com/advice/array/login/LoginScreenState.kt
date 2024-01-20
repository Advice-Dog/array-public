package com.advice.array.login

import com.advice.array.api.response.LoginResponse

sealed class LoginScreenState {

    object Init : LoginScreenState()
    object Loading : LoginScreenState()
    class Error(val ex: Exception) : LoginScreenState()
    class Success(val data: LoginResponse) : LoginScreenState()

}