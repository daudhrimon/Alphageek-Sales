package com.gdm.alphageeksales.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdm.alphageek.data.local.down_sync.RoutePlanDetails
import com.gdm.alphageeksales.data.local.down_sync.*
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit

@Database(
    entities = [
        Brand::class,
        Brief::class,
        Dashboard::class,
        Outlet::class,
        OutletChannel::class,
        OutletType::class,
        Product::class,
        RoutePlan::class,
        LocationDownSync::class,
        Schedule::class,
        Posm::class,
        ScheduleVisit::class,
        Checkout::class,
        Inventory::class,
        Order::class,
        OrderDetails::class,
        AppSetting::class,
        RoutePlanDetails::class,
        PaymentType::class,
        Questions::class
    ],
    version = 18,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAppDao(): AppDao
}
