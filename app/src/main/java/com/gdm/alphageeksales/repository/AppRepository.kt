package com.gdm.alphageeksales.repository

import com.gdm.alphageek.data.local.down_sync.RoutePlanDetails
import com.gdm.alphageeksales.api.ApiService
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit
import com.gdm.alphageeksales.database.AppDao
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import javax.inject.Inject

class AppRepository @Inject constructor(private val appDao: AppDao,private val apiService: ApiService) {
    suspend fun userLocation(gio_lat: String, gio_long: String,address:String) = apiService.liveTrac(gio_lat, gio_long,address,2)
    suspend fun getLGAList(stateID:String) = apiService.getLGAList(stateID)
    suspend fun getEducationList() = apiService.getEducationList()
    suspend fun getBankList() = apiService.getBankList()
    suspend fun getDocumentTypeList() = apiService.getDocumentTypeList()
    suspend fun checkoutRequest(responseBody:String) = apiService.checkOutRequest(responseBody)
    suspend fun inventoryRequest(responseBody:String) = apiService.inventoryRequest(responseBody)
    suspend fun userLogin(email: String, password: String,deviceID:String,loginAddress: String) = apiService.login(email, password,deviceID,loginAddress,2)
    suspend fun signUp(name: String,email: String, password: String,deviceID:String,ip_address:String) = apiService.signUP(name,email, password,deviceID,ip_address)
    suspend fun userLogout(ip_address: String,loginAddress: String) = apiService.logout(ip_address,loginAddress)
    suspend fun getCountryList() = apiService.getCountryList()
    suspend fun getStateList(countryID:String) = apiService.getStateList(countryID)
    suspend fun getProfileInfo() = apiService.getProfileInfo()
    suspend fun getDownSyncInfo() = apiService.downSync()

    suspend fun updateProfile(
        firstname: RequestBody,lastname: RequestBody,
        middle_name: RequestBody,gender: RequestBody,
        phone: RequestBody,address: RequestBody,
        country_id: RequestBody,state_id: RequestBody,
        lga: RequestBody,nin: RequestBody,bvn: RequestBody,
        lasra: RequestBody,education: RequestBody,bank_id: RequestBody,
        account_name: RequestBody,account_number: RequestBody,
        guarantor_name: RequestBody,guarantor_email: RequestBody,
        guarantor_phone: RequestBody,guarantor_id_type: RequestBody,
        guarantor_Document: MultipartBody.Part?,userImage: MultipartBody.Part?
    ) = apiService.updateProfile(
        firstname,lastname,middle_name,gender,
        phone,address,country_id,state_id,lga,nin,bvn,lasra,education,
        bank_id,account_name,account_number,guarantor_name,guarantor_email,
        guarantor_phone,guarantor_id_type,guarantor_Document,userImage
    )

    // up sync
    suspend fun upSyncData(@Body body: RequestBody) = apiService.upSyncData(body)


    // local db operations for down sync
    suspend fun insertCheckOutList(checkoutList: List<Checkout>) = appDao.insertCheckOutList(checkoutList)
    suspend fun insertBrandList(brandList: List<Brand>) = appDao.insertBrandList(brandList)
    suspend fun insertAppSettings(appsettings: AppSetting) = appDao.insertApp(appsettings)
    suspend fun insertOrderList(inventory: List<Order>) = appDao.insertOrderList(inventory)
    suspend fun insertOrderItem(order: Order) = appDao.insertOrderItem(order)
    suspend fun insertOrderDetails(inventory: List<OrderDetails>) = appDao.insertOrderDetails(inventory)
    suspend fun insertInventoryList(inventory: List<Inventory>) = appDao.insertInventoryList(inventory)
    suspend fun insertBriefList(data: List<Brief>) = appDao.insertBriefList(data)
    suspend fun insertDashBoardData(dashboard: Dashboard) = appDao.insertDashBoardDataList(dashboard)
    suspend fun insertOutletList(data: List<Outlet>) = appDao.insertOutletList(data)
    suspend fun insertOutletChannelList(data: List<OutletChannel>) = appDao.insertOutletChannelList(data)
    suspend fun insertOutletType(data: List<OutletType>) = appDao.insertOutletType(data)
    suspend fun insertProductList(data: List<Product>) = appDao.insertProductList(data)
    suspend fun insertRoutePlan(data: List<RoutePlan>) = appDao.insertRoutePlan(data)
    suspend fun insertRoutePlanDetails(data: List<RoutePlanDetails>) = appDao.insertRoutePlanDetails(data)
    suspend fun insertLocation(data: List<LocationDownSync>) = appDao.insertLocation(data)
    suspend fun insertSchedule(data: List<Schedule>) = appDao.insertSchedule(data)
    suspend fun insertPosmProduct(data: List<Posm>) = appDao.insertPosmProduct(data)
    suspend fun insertPaymentTypes(data: List<PaymentType>?) = appDao.insertPaymentTypes(data)
    suspend fun insertQuestions(data: List<Questions>?) = appDao.insertQuestions(data)


    // local db operations for data deletion
    suspend fun deleteOrderList()           = appDao.deleteOrderList()
    suspend fun deleteAppSettings()         = appDao.deleteAppSettings()
    suspend fun deleteOrderDetails()        = appDao.deleteOrderDetails()
    suspend fun deleteCheckoutList()        = appDao.deleteCheckoutList()
    suspend fun deleteBrandList()           = appDao.deleteBrandList()
    suspend fun deleteBriefList()           = appDao.deleteBriefList()
    suspend fun deleteDashBoardData()       = appDao.deleteDashBoardDataList()
    suspend fun deleteOutletList()          = appDao.deleteOutletList()
    suspend fun deleteOutletChannelList()   = appDao.deleteOutletChannelList()
    suspend fun deleteOutletType()          = appDao.deleteOutletType()
    suspend fun deleteProductList()         = appDao.deleteProductList()
    suspend fun deleteRoutePlan()           = appDao.deleteRoutePlan()
    suspend fun deleteLocation()            = appDao.deleteLocation()
    suspend fun deleteSchedule()            = appDao.deleteSchedule()
    suspend fun deletePosmProduct()         = appDao.deletePosmProduct()
    suspend fun deleteVisitData()           = appDao.deleteVisitData()
    suspend fun deleteInventoryData()       = appDao.deleteInventory()
    suspend fun deletePaymentTypes()        = appDao.deletePaymentTypes()
    suspend fun deleteQuestions()           = appDao.deleteQuestions()

    // fetch from local database
    fun getDashboardData() = appDao.getDashBoardInfo()
    fun getBriefList() = appDao.getBriefList()
    fun getRoutePlan() = appDao.getRoutePlan()
    fun getAllLocation() = appDao.getAllLocation()
    fun getRoutePlanDetails(dayID:Int) = appDao.getAllRouteDetail(dayID)
    fun getAllOfflineScheduleList() = appDao.getAllOfflineScheduleList()
    fun getScheduleVisitData() = appDao.getScheduleVisitData()


    // outlet info
    fun getAllOutlet()                     = appDao.getAllOutLet()
    fun getAllOfflineOutlet()              = appDao.getAllOfflineOutlet()
    fun getAllUpdatableOutlet()            = appDao.getAllUpdatableOutlet()
    fun getOutletById(outletID:Long?)       = appDao.getOutletById(outletID)
    fun insertOutlet(outlet: Outlet)       = appDao.insertOutlet(outlet)
    fun updateOutlet(outlet: Outlet)       = appDao.updateOutlet(outlet)
    fun getOutletTypes()                   = appDao.getOutLetType()
    fun getOutLetChannel()                 = appDao.getOutLetChannel()

    // schedule
    fun insertSchedule(schedule: Schedule) = appDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: Schedule) = appDao.updateSchedule(schedule)
    fun getScheduleList(date:String) = appDao.getScheduleList(date)
    fun updateDashboard(schedule: Dashboard) = appDao.updateDashBoardData(schedule)
    suspend fun updateInventory(inventories:List<Inventory>) = appDao.updateInventory(inventories)

    // visit
    fun insertVisitData(scheduleVisit: ScheduleVisit) = appDao.insertScheduleVisit(scheduleVisit)

    // product and brand
    fun getProductByBrand(brandID:Int?) = appDao.getProductByBrand(brandID)
    fun getAllProduct() = appDao.getAllProduct()
    fun getCheckoutList() = appDao.getCheckoutList()
    fun getInventoryList() = appDao.getInventoryList()
    fun getInventoryListById(brandID: Int?) = appDao.getInventoryListById(brandID)
    fun getOrderList() = appDao.getOrderList()
    fun getOrderListByType(orderType: Int) = appDao.getOrderListByType(orderType)
    fun getOrderItemById(orderId: Long?) = appDao.getOrderItemById(orderId)
    fun getSettings() = appDao.getSettings()
    fun getOrderDetailById(orderId: Long?) = appDao.getOrderDetailById(orderId)
    fun getAllBrand() = appDao.getAllBrand()
    fun getPaymentTypes() = appDao.getPaymentTypes()
    fun getQuestions() = appDao.getQuestions()
}
