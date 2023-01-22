package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outlet_type")
data class OutletType(
    @PrimaryKey
    val id: Int,
    val type_name: String
){
    override fun toString(): String {
        return type_name
    }
}