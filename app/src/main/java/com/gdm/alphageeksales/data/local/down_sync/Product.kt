package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class Product(
    @PrimaryKey
    val id: Int,
    val brand_id: Int?,
    val category_id: Int?,
    val client_id: Int?,
    val client_name: String?,
    val product_image: String?,
    val product_name: String?,
    val rec_retail_price: Double?,
    val sales_price: Double?,
    val unit_per_case: Int?,
    val unit_price: Double?,
    var case_qty: Int?  = null,
    var unit_qty: Int?  = null
)