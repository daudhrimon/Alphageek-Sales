package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageeksales.data.remote.BaseResponse
import com.gdm.alphageeksales.repository.AppRepository
import com.gdm.alphageeksales.data.remote.bank.BankResponse
import com.gdm.alphageeksales.data.remote.country.CountryResponse
import com.gdm.alphageeksales.data.remote.document_type.DocumentTypeResponse
import com.gdm.alphageeksales.data.remote.education.EducationResponse
import com.gdm.alphageeksales.data.remote.lga.LgaResponse
import com.gdm.alphageeksales.data.remote.login.LoginResponse
import com.gdm.alphageeksales.data.remote.state.StateResponse
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class InformationViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val stateListResponse = MutableLiveData<StateResponse>()
    val countryListResponse = MutableLiveData<CountryResponse>()
    val lgaResponse = MutableLiveData<LgaResponse>()
    val bankResponse = MutableLiveData<BankResponse>()
    val educationResponse = MutableLiveData<EducationResponse>()
    val documentTypeResponse = MutableLiveData<DocumentTypeResponse>()
    val loading = MutableLiveData<Boolean>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { onError(it) }
    }

    fun getCountryList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getCountryList()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    countryListResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun getStateList(countryID: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getStateList(countryID)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    stateListResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun getLgaList(stateID: String) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getLGAList(stateID)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    lgaResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun getBankList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getBankList()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    bankResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun getEducationList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getEducationList()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    educationResponse.postValue(response.body())
                    loading.value = false
                } else {
                    onError(response.errorBody()!!.string())
                }
            }
        }
    }

    fun getDocumentType() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getDocumentTypeList()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    documentTypeResponse.postValue(response.body())
                    loading.value = false
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