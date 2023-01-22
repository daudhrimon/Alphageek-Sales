package com.gdm.alphageeksales.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gdm.alphageek.data.local.down_sync.RoutePlanDetails
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit
import com.gdm.alphageeksales.data.remote.BaseResponse
import com.gdm.alphageeksales.repository.AppRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

@HiltViewModel
class DataSyncViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val errorMessage = MutableLiveData<String>()
    val downSyncResponse = MutableLiveData<Boolean>()
    val upSyncResponse = MutableLiveData<Boolean>()
    val localOutletList = MutableLiveData<List<Outlet>>()
    val updatableOutletList = MutableLiveData<List<Outlet>>()
    val offlineScheduleList = MutableLiveData<List<Schedule>>()
    val scheduleVisitList = MutableLiveData<List<ScheduleVisit>>()
    val orderGenResponse = MutableLiveData<String>()
    val startLogout = MutableLiveData<Boolean>()
    var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { errorMessage.postValue("Something went wrong !") }
    }

    private val syncExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { onError(it) }
    }

    private val ordersExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.localizedMessage?.let { orderGenResponse.postValue(it) }
    }


    fun userLocation(gio_lat:String,gio_long: String,address:String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            repository.userLocation(gio_lat, gio_long,address)
        }
    }

    fun getAllOfflineOutlet() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllOfflineOutlet()
            withContext(Dispatchers.Main) {
                localOutletList.postValue(data)
            }
        }
    }

    fun getAllUpdatableOutlet() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllUpdatableOutlet()
            withContext(Dispatchers.Main) {
                updatableOutletList.postValue(data)
            }
        }
    }

    fun getAllOfflineScheduleList() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getAllOfflineScheduleList()
            withContext(Dispatchers.Main) {
                offlineScheduleList.postValue(data)
            }
        }
    }

    fun getScheduleVisitData() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val data = repository.getScheduleVisitData()
            withContext(Dispatchers.Main) {
                scheduleVisitList.postValue(data)
            }
        }
    }

    fun saveOrdersLocalDb(order: Order,
                          orderDetails: List<OrderDetails>,
                          schedule: Schedule) {
        job = CoroutineScope(Dispatchers.IO + ordersExceptionHandler).launch {
            repository.insertOrderItem(order)
            repository.insertOrderDetails(orderDetails)
            repository.updateSchedule(schedule)
            withContext(Dispatchers.Main) {
                orderGenResponse.postValue("success")
            }
        }
    }

    fun upSync(@Body body: RequestBody) {
        job = CoroutineScope(Dispatchers.IO + syncExceptionHandler).launch {
            val response = repository.upSyncData(body)
            withContext(Dispatchers.Main) {
                when { response.isSuccessful && response.body() != null -> {
                        when { response.body()!!.success -> deleteInfoLocalDb()
                            else -> response.body()?.message?.let { errorMessage.postValue(it) }}
                } else -> response.errorBody()?.let{onError(it.toString())} }
            }
        }
    }

    fun downSync() {
        job = CoroutineScope(Dispatchers.IO + syncExceptionHandler).launch {
            val response = repository.getDownSyncInfo()
            withContext(Dispatchers.Main) {
                when { response.isSuccessful && response.body() != null -> {
                        when { response.body()!!.success -> saveInfoLocalDb(response.body()!!.data)
                            else -> response.body()?.message?.let { errorMessage.postValue(it) }}
                } else -> response.errorBody()?.let{onError(it.toString())} }
            }
        }
    }

    private fun saveInfoLocalDb(downSyncData: DownSyncData){
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val routePlanList = ArrayList<RoutePlan>(ArrayList(emptyList()))
            val routePlanDetailList = ArrayList<RoutePlanDetails>(ArrayList(emptyList()))
            downSyncData.route_plane?.forEach {
                it.details?.forEach { det->
                    val routePlanDetail = RoutePlanDetails(
                        0,
                        it.id,
                        det.location_id,
                        det.location_name,
                        det.gio_lat,
                        det.gio_long
                    )
                    routePlanDetailList.add(routePlanDetail)
                }
                routePlanList.add(RoutePlan(it.id, it.day_of_week))
            }.also {
                repository.insertRoutePlan(routePlanList)
                repository.insertRoutePlanDetails(routePlanDetailList)
            }
            val orderList = ArrayList<Order>(ArrayList(emptyList()))
            val orderDetailList = ArrayList<OrderDetails>(ArrayList(emptyList()))
            downSyncData.orderList?.forEach {
                val orderItem = Order(
                    id = it.order_id,
                    order_id = it.order_id,
                    schedule_id = it.schedule_id,
                    outlet_id = it.outlet_id,
                    country_id = it.country_id,
                    region_id = it.region_id,
                    state_id = it.state_id,
                    location_id = it.location_id,
                    grand_total = it.grand_total,
                    paid_amount = it.paid_amount,
                    due_amount = it.due_amount,
                    change_amount = it.change_amount,
                    payment_type = it.payment_type,
                    order_type = it.order_type,
                    order_date = it.order_date,
                    image_url = it.image_url,
                    outlet_name = it.outlet_name,
                    user_name = it.user_name,
                    outlet_phone = it.outlet_phone
                )
                orderList.add(orderItem)
                orderDetailList.addAll(it.order_ditails)
            }.also {
                repository.insertOrderList(orderList)
                repository.insertOrderDetails(orderDetailList)
            }
            downSyncData.dashboard?.let {repository.insertDashBoardData(it)}
            downSyncData.app_setting?.let {repository.insertAppSettings(it)}
            downSyncData.brand_list?.let {repository.insertBrandList(it)}
            downSyncData.schedules?.let {repository.insertSchedule(it)}
            downSyncData.location?.let {repository.insertLocation(it)}
            downSyncData.outlets?.let {repository.insertOutletList(it)}
            downSyncData.outlet_channels?.let {repository.insertOutletChannelList(it)}
            downSyncData.outlet_types?.let {repository.insertOutletType(it)}
            downSyncData.checkout?.let {repository.insertCheckOutList(it)}
            downSyncData.products?.let {repository.insertProductList(it)}
            downSyncData.inventory?.let {repository.insertInventoryList(it)}
            downSyncData.brifs?.let {repository.insertBriefList(it)}
            downSyncData.posms_product_list?.let {repository.insertPosmProduct(it)}
            downSyncData.payment_types?.let {repository.insertPaymentTypes(it)}
            downSyncData.questions?.let {repository.insertQuestions(it)}
            withContext(Dispatchers.Main) {
                downSyncResponse.postValue(true)
            }
        }
    }

    fun deleteInfoLocalDb() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            repository.deleteAppSettings()
            repository.deleteOrderList()
            repository.deleteOrderDetails()
            repository.deleteCheckoutList()
            repository.deleteBrandList()
            repository.deleteBriefList()
            repository.deleteDashBoardData()
            repository.deleteOutletList()
            repository.deleteOutletChannelList()
            repository.deleteOutletType()
            repository.deleteProductList()
            repository.deleteRoutePlan()
            repository.deleteLocation()
            repository.deleteSchedule()
            repository.deleteVisitData()
            repository.deleteInventoryData()
            repository.deletePosmProduct()
            repository.deletePaymentTypes()
            repository.deleteQuestions()
            withContext(Dispatchers.Main) {
                upSyncResponse.postValue(true)
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
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}