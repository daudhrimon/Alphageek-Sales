package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageeksales.data.local.down_sync.Outlet
import com.gdm.alphageeksales.data.local.down_sync.OutletChannel
import com.gdm.alphageeksales.data.local.down_sync.OutletType
import com.gdm.alphageeksales.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class ImageNoteViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val outletChannel = MutableLiveData<List<OutletChannel>>()
    val outlet = MutableLiveData<Outlet>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    fun getOutletById(outletID:Long?) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOutletById(outletID)
            withContext(Dispatchers.Main) {
                outlet.postValue(data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}