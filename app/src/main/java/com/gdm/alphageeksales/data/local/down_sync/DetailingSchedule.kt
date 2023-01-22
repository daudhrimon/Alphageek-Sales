package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "detailed_schedule")
data class DetailingSchedule(
    @PrimaryKey
    val schedule_id: Long,
    val outlet_id: Long,
    val gio_lat: String?,
    val gio_long: String?,
    val location_name: String?,
    val outlet_address: String?,
    val outlet_name: String?,
    val schedule_date: String?,
    val schedule_time: String?,
    val reps: String?,
    val topics: String?,
    val country_id: Int?,
    val state_id: Int?,
    val region_id: Int?,
    val location_id: Int?,
    var is_local:Int = 0,
    var visit_status: Int = 0
)
