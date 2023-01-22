package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outlet")
data class Outlet(
    @PrimaryKey
    var outlet_id: Long,
    val channel_id: Int,
    val gio_lat: String?,
    val gio_long: String?,
    val outlet_address: String?,
    val outlet_image: String?,
    val outlet_name: String?,
    val outlet_phone: String?,
    val country_id: Int?,
    val state_id: Int?,
    val region_id: Int?,
    val location_id: Int?,
    val location_name: String?,
    val type_id: Int?,
    val cpf_name: String?,
    val cpl_name: String?,
    val street_name: String?,
    val street_no: String?,
    val cpp: String?,
    val is_bso: Int = 0,
    var is_local: Int = 0,
    var is_up: Int = 0,
    var note: String?,
    val answers: List<Answers?>?
){
    override fun toString(): String {
        return outlet_name.toString()
    }
}