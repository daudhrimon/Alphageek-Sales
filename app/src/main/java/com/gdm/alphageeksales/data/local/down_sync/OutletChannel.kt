package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outlet_channel")
data class OutletChannel(
    @PrimaryKey
    val id: Int,
    val channel_name: String
){
    override fun toString(): String {
        return channel_name
    }
}