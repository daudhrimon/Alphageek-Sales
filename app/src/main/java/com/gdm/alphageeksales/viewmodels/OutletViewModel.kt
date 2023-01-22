package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageeksales.data.local.down_sync.Outlet
import com.gdm.alphageeksales.data.local.down_sync.OutletChannel
import com.gdm.alphageeksales.data.local.down_sync.OutletType
import com.gdm.alphageeksales.data.local.down_sync.Questions
import com.gdm.alphageeksales.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class OutletViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val outletChannel = MutableLiveData<List<OutletChannel>>()
    val outletTypes = MutableLiveData<List<OutletType>>()
    val outletList = MutableLiveData<List<Outlet>>()
    val outlet = MutableLiveData<Outlet>()
    val insertOutlet = MutableLiveData<Long>()
    val updateOutlet = MutableLiveData<Int>()
    val questionsData = MutableLiveData<List<Questions>>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    fun insertNewOutlet(outlet: Outlet) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.insertOutlet(outlet)
            withContext(Dispatchers.Main) {
                insertOutlet.postValue(data)
            }
        }
    }

    fun updateOutlet(outlet: Outlet) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.updateOutlet(outlet)
            withContext(Dispatchers.Main) {
                updateOutlet.postValue(data)
            }
        }
    }

    fun getAllOutlet() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllOutlet()
            withContext(Dispatchers.Main) {
                outletList.postValue(data)
            }
        }
    }



    fun getOutletById(outletID:Long) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOutletById(outletID)
            withContext(Dispatchers.Main) {
                outlet.postValue(data)
            }
        }
    }

    fun getOutletChannel() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOutLetChannel()
            withContext(Dispatchers.Main) {
                outletChannel.postValue(data)
            }
        }
    }

    fun getOutletTypes() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOutletTypes()
            withContext(Dispatchers.Main) {
                outletTypes.postValue(data)
            }
        }
    }

    fun getQuestions() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getQuestions()
            withContext(Dispatchers.Main) {
                questionsData.postValue(data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}