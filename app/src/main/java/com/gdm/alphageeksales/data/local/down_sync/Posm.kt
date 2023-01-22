package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posm")
data class Posm(
    @PrimaryKey
    val id: Int,
    val brand_id: Int,
    val category_id: Int,
    val client_id: Int,
    val client_name: String,
    val product_image: String?,
    val product_name: String,
    val product_weight: String
)