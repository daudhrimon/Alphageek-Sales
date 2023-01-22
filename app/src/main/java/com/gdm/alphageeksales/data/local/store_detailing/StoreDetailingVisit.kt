package com.gdm.alphageeksales.data.local.store_detailing

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_detail_data")
data class StoreDetailingData(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val schedule_id: Long,
    val status: String,
    val note: String,
    val outlet_name: String,
    val outlet_id: Long,
    val outlet_address: String?,
    val is_local: Int = 0,   // local = 0, server = 1
    val is_insert: Int = 0  // insert =0, update =1
)
