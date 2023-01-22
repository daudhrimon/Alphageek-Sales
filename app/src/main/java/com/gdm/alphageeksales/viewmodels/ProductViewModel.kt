package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val brandList   = MutableLiveData<List<Brand>>()
    val productList   = MutableLiveData<List<Product>>()
    val productListAll   = MutableLiveData<List<Product>>()
    val checkOutList   = MutableLiveData<List<Checkout>>()
    val inventoryList   = MutableLiveData<List<Inventory>>()
    val inventoryByBrand   = MutableLiveData<List<Inventory>>()
    val orderList   = MutableLiveData<List<Order>>()
    val orderItem   = MutableLiveData<Order>()
    val appSettings   = MutableLiveData<AppSetting>()
    val orderDetails   = MutableLiveData<List<OrderDetails>>()
    val orderDetailsById   = MutableLiveData<List<OrderDetails>>()
    val paymentTypes   = MutableLiveData<List<PaymentType>>()
    val loading   = MutableLiveData<Boolean>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let {
            errorMessage.postValue("Something went wrong !")
            loading.postValue(false)
        }
    }

    fun getProductByBrand(brandID:Int?) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getProductByBrand(brandID)
            withContext(Dispatchers.Main) {
                productList.postValue(data)
            }
        }
    }

    fun getAllProducts() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllProduct()
            withContext(Dispatchers.Main) {
                productListAll.postValue(data)
            }
        }
    }

    fun getAppSettings() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getSettings()
            withContext(Dispatchers.Main) {
                appSettings.postValue(data)
            }
        }
    }

    fun getOrderList() {
        loading.postValue(true)
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOrderList()
            withContext(Dispatchers.Main) {
                orderList.postValue(data)
                //loading.postValue(false)
            }
        }
    }

    fun getOrderListByType(orderType: Int) {
        loading.postValue(true)
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOrderListByType(orderType)
            withContext(Dispatchers.Main) {
                orderList.postValue(data)
                //loading.postValue(false)
            }
        }
    }

    fun getOrderItemById(orderId: Long?) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOrderItemById(orderId)
            withContext(Dispatchers.Main) {
                orderItem.postValue(data)
            }
        }
    }

    fun getOrderDetailsById(orderId: Long?) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getOrderDetailById(orderId)
            withContext(Dispatchers.Main) {
                orderDetailsById.postValue(data)
            }
        }
    }
    fun getInventoryList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getInventoryList()
            withContext(Dispatchers.Main) {
                inventoryList.postValue(data)
            }
        }
    }
    fun getInventoryByBrand(brandID: Int?) {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getInventoryListById(brandID)
            withContext(Dispatchers.Main) {
                inventoryByBrand.postValue(data)
            }
        }
    }
    fun getCheckOUtList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getCheckoutList()
            withContext(Dispatchers.Main) {
                checkOutList.postValue(data)
            }
        }
    }

    fun getBrandList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllBrand()
            withContext(Dispatchers.Main) {
                brandList.postValue(data)
            }
        }
    }

    fun getPaymentTypes() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getPaymentTypes()
            withContext(Dispatchers.Main) {
                paymentTypes.postValue(data)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}