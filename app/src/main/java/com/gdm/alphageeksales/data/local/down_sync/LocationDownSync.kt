package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationDownSync(
    @PrimaryKey(autoGenerate = true)
    val idm: Int,
    val location_id: Int,
    val country_id: Int?,
    val state_id: Int?,
    val region_id: Int?,
    val location_name: String?,
    val gio_lat: String?,
    val gio_long: String?

){
    override fun toString(): String {
        return location_name!!
    }
}