package com.ryan.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    companion object {
        val TAG = LoginViewModel::class.java.simpleName
    }

    val loginState = MutableLiveData<Boolean>()

    fun getLoginState() {

    }
}