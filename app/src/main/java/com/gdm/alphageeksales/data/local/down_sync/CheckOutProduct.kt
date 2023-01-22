package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkout")

class CheckOutProduct
    (
    @PrimaryKey(autoGenerate = true)

    val  id:Int,
    val checkout_date: String,
    val is_confirm: Int,
    val product_id: Int,
    val product_name: String?,
    val category_id: Int,
    val brand_id: Int,
    val sales_price: Int,
    val product_image: String?,
    val case_qty: Int,
    val unit_qty: Int

    )