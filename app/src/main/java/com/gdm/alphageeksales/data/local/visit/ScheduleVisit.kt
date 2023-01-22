package com.gdm.alphageeksales.data.local.visit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_visit")
data class ScheduleVisit(
    @PrimaryKey
    val id:Long,
    val schedule_id: Long,
    val outlet_id: Long?,
    val outlet_type_id: Int?,
    val outlet_channel_id: Int?,
    val visit_date: String?,
    val visit_time: String?,
    val country_id: Int?,
    val state_id: Int?,
    val region_id: Int?,
    val location_id: Int?,
    val visit_type: Int?,
    val image_list: String? = null,
    val notes: String? = null,
    val planogram_list: String? = null,
    val available_list: String? = null,
    val posm_tracking_list: String? = null,
    val posm_deploy_list: String? = null,
    val promo_description_list: String? = null,
    val store_detailing_visit: String? = null,

    val order_id:String?= null,
    val order_type:String?= null,
    val payment_type:String?= null,
    val totalsale:String?= null,
    val paid_amount:String?= null,
    val due_amount:String?= null,
    val change_amount:String?= null,
    val sales_order_list:String?= null,
    val order_date:String?= null,
    val order_time:String?= null,

    var pre_schedule_id: Long?=0,
    var pre_order_id: Long?=0,
    var schedule_type: Int?=0,


    var stat_time: String? = null,
    var end_time: String? = null,
    var gio_lat: String? = null,
    var gio_long: String? = null,
    var is_exception: String? = null,
    var visit_distance: String? = null,
    var isInternetAvailable: Boolean? = false,

    )
