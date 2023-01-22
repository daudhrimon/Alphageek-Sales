package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
class Inventory(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val category_id: Int?,
    val client_id: Int?,
    val client_name: String?,
    val product_name: String?,
    val product_image: String?,
    val product_id: Int?,
    val brand_id: Int?,
    val unit_per_case: Int?,
    val unit_price: Double?,
    var case_qty: Int?,
    var unit_qty: Int?
)