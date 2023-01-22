package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageeksales.repository.AppRepository
import com.gdm.alphageeksales.data.remote.BaseResponse
import com.gdm.alphageeksales.data.remote.profile.ProfileResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val profileResponse = MutableLiveData<ProfileResponse>()
    val profileUpdateResponse = MutableLiveData<BaseResponse>()
    val logoutResponse = MutableLiveData<BaseResponse>()
    val loading = MutableLiveData<Boolean>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { onError(it) }
    }

    fun getProfileInfo() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getProfileInfo()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    profileResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun updateProfile(
        firstname: RequestBody, lastname: RequestBody,
        middle_name: RequestBody, gender: RequestBody,
        phone: RequestBody, address: RequestBody,
        country_id: RequestBody, state_id: RequestBody,
        lga: RequestBody, nin: RequestBody, bvn: RequestBody,
        lasra: RequestBody, education: RequestBody, bank_id: RequestBody,
        account_name: RequestBody, account_number: RequestBody,
        guarantor_name: RequestBody, guarantor_email: RequestBody,
        guarantor_phone: RequestBody, guarantor_id_type: RequestBody,
        guarantor_Document: MultipartBody.Part?, userImage: MultipartBody.Part?
    ) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.updateProfile(
                firstname,lastname,middle_name,gender,
                phone,address,country_id,state_id,lga,nin,bvn,lasra,education,
                bank_id,account_name,account_number,guarantor_name,guarantor_email,
                guarantor_phone,guarantor_id_type,guarantor_Document,userImage
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    profileUpdateResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun userLogout(ip_address: String,loginAddress: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.userLogout(ip_address,loginAddress)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    logoutResponse.postValue(response.body())
                } else {
                    onError(response.errorBody()!!.string())
                }
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