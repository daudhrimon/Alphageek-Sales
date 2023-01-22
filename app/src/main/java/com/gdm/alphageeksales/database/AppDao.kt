package com.gdm.alphageeksales.database

import androidx.room.*
import com.gdm.alphageek.data.local.down_sync.RoutePlanDetails
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit

@Dao
interface AppDao {

    // down sync operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckOutList(checkout : List<Checkout>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderList(Order : List<Order>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(Order : Order)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderDetails(orderDetails : List<OrderDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryList(inventory : List<Inventory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrandList(brandList: List<Brand>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(brandList: AppSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBriefList(data: List<Brief>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashBoardDataList(dashboard: Dashboard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutletList(data: List<Outlet>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutletChannelList(data: List<OutletChannel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutletType(data: List<OutletType>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductList(data: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePlan(data: List<RoutePlan>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePlanDetails(data : List<RoutePlanDetails>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(data: List<LocationDownSync>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(data: List<Schedule>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosmProduct(data: List<Posm>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentTypes(data: List<PaymentType>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(data: List<Questions>?)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDashBoardData(dashboard: Dashboard):Int


    // delete sync operations
    @Query("DELETE FROM checkout")
    suspend fun deleteCheckoutList()

    @Query("DELETE FROM orderlist")
    suspend fun deleteOrderList()

    @Query("DELETE FROM order_ditails")
    suspend fun deleteOrderDetails()

    @Query("DELETE FROM brand")
    suspend fun deleteBrandList()

    @Query("DELETE FROM brief")
    suspend fun deleteBriefList()

    @Query("DELETE FROM dashboard")
    suspend fun deleteDashBoardDataList()

    @Query("DELETE FROM outlet")
    suspend fun deleteOutletList()

    @Query("DELETE FROM outlet_channel")
    suspend fun deleteOutletChannelList()

    @Query("DELETE FROM outlet_type")
    suspend fun deleteOutletType()

    @Query("DELETE FROM product")
    suspend fun deleteProductList()

    @Query("DELETE FROM route_plan")
    suspend fun deleteRoutePlan()

    @Query("DELETE FROM location")
    suspend fun deleteLocation()

    @Query("DELETE FROM schedule")
    suspend fun deleteSchedule()

    @Query("DELETE FROM app_setting")
    suspend fun deleteAppSettings()

    @Query("DELETE FROM posm")
    suspend fun deletePosmProduct()

    @Query("DELETE FROM schedule_visit")
    suspend fun deleteVisitData()

    @Query("DELETE FROM inventory")
    suspend fun deleteInventory()

    @Query("DELETE FROM payment_type")
    suspend fun deletePaymentTypes()

    @Query("DELETE FROM questions")
    suspend fun deleteQuestions()

    // dashboard information
    @Query("select * from Dashboard")
    fun getDashBoardInfo(): Dashboard

    @Query("select * from app_setting")
    fun getSettings(): AppSetting

    @Query("select * from schedule where schedule_date=:date")
    fun getScheduleList(date:String): List<Schedule>

    @Query("select * from brief")
    fun getBriefList(): List<Brief>

    @Query("select * from route_plan")
    fun getRoutePlan(): List<RoutePlan>

    @Query("select * from location")
    fun getAllLocation(): List<LocationDownSync>

    @Query("select * from route_plan_details where rootId = :rootIId")
    fun getAllRouteDetail(rootIId : Int): List<RoutePlanDetails>
    // schedule
    @Query("select * from schedule where is_local = 1")
    fun getAllOfflineScheduleList(): List<Schedule>

    @Query("select * from schedule_visit")
    fun getScheduleVisitData(): List<ScheduleVisit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSchedule(schedule: Schedule):Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSchedule(schedule: Schedule):Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScheduleVisit(scheduleVisit: ScheduleVisit):Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateInventory(inventory: List<Inventory>):Int


    // outlet
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOutlet(outlet: Outlet):Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateOutlet(outlet: Outlet):Int

    @Query("select * from outlet")
    fun getAllOutLet(): List<Outlet>

    @Query("select * from outlet where is_local = 1")
    fun getAllOfflineOutlet(): List<Outlet>

    @Query("select * from outlet where is_up = 1")
    fun getAllUpdatableOutlet(): List<Outlet>

    @Query("select * from outlet where outlet_id = :outletID")
    fun getOutletById(outletID:Long?): Outlet

    @Query("select * from outlet_type")
    fun getOutLetType(): List<OutletType>

    @Query("select * from outlet_channel")
    fun getOutLetChannel(): List<OutletChannel>

    // product and brand
    @Query("select * from brand")
    fun getAllBrand():List<Brand>

    @Query("select * from inventory")
    fun getInventoryList():List<Inventory>

    @Query("select * from inventory where brand_id = :brandID")
    fun getInventoryListById(brandID: Int?):List<Inventory>

    @Query("select * from orderlist")
    fun getOrderList():List<Order>

    @Query("select * from orderlist where order_type = :orderType")
    fun getOrderListByType(orderType: Int):List<Order>

    @Query("select * from orderlist where order_id = :orderId")
    fun getOrderItemById(orderId: Long?): Order

    @Query("select * from order_ditails where order_id = :orderId")
    fun getOrderDetailById(orderId: Long?): List<OrderDetails>

    @Query("select * from product where brand_id = :brandID")
    fun getProductByBrand(brandID:Int?): List<Product>

    @Query("select * from product")
    fun getAllProduct(): List<Product>

    @Query("select * from checkout")
    fun getCheckoutList(): List<Checkout>

    @Query("select * from payment_type")
    fun getPaymentTypes(): List<PaymentType>

    @Query("select * from questions")
    fun getQuestions(): List<Questions>
}