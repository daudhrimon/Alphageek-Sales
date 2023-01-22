package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageeksales.data.local.down_sync.Inventory
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit
import com.gdm.alphageeksales.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class VisitViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val insertVisitResponse = MutableLiveData<Long>()
    val loading = MutableLiveData<Boolean>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    fun insertVisitData(scheduleVisit: ScheduleVisit) {
        loading.postValue(true)
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.insertVisitData(scheduleVisit)
            withContext(Dispatchers.Main) {
                insertVisitResponse.postValue(data)
                loading.postValue(false)
            }
        }
    }

    fun updateInventory(inventories:List<Inventory>) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.updateInventory(inventories)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}