package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brand")
data class Brand(
    @PrimaryKey
    val id: Int,
    val brand_name: String
) {
    override fun toString(): String {
        return brand_name
    }
}