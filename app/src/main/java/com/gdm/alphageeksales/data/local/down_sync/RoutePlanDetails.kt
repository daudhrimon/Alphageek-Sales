package com.gdm.alphageek.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_plan_details")
class RoutePlanDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val rootId: Int,
    var location_id: Int,
    val location_name: String?,
    val gio_lat: String?,
    val gio_long: String?,
)