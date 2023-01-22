package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageeksales.data.remote.BaseResponse
import com.gdm.alphageeksales.data.remote.InventoryResponse
import com.gdm.alphageeksales.repository.AppRepository
import com.gdm.alphageeksales.data.remote.login.LoginResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val loginResponse = MutableLiveData<LoginResponse>()
    val getInventoryResponse = MutableLiveData<InventoryResponse>()
    val checkoutResponse = MutableLiveData<InventoryResponse>()
    val signUpResponse = MutableLiveData<LoginResponse>()
    val loading = MutableLiveData<Boolean>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { onError(it) }
    }

    fun userLogin(email:String,password: String,deviceID:String,loginAddress: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.userLogin(email,password,deviceID,loginAddress)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    loginResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun checkOutRequest(reQuestBody:String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.checkoutRequest(reQuestBody)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    checkoutResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun inventoryRequest(reQuestBody:String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.inventoryRequest(reQuestBody)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    getInventoryResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun userSignUp(name:String,email:String,password: String,deviceID:String,ipAddress:String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.signUp(name,email, password,deviceID,ipAddress)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    signUpResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())                }
            }
        }
    }

    private fun onError(response: String) {
        try {
            val error = Gson().fromJson(response, BaseResponse::class.java)
            errorMessage.postValue(error.message)
        }catch (e:Exception){
            errorMessage.postValue("Your internet connection may be unstable !")
        }
        loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}