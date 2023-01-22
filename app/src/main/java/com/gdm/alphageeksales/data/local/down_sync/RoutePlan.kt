package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "route_plan")
data class RoutePlan(
    @PrimaryKey
    val id: Int,
    val day_of_week: String
)