package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_ditails")
data class OrderDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var product_id:Int?,
    var order_id:Long?,
    var unit_per_case:Int?,
    var price:Double?,
    var product_name:String?,
    var category_id:Int?,
    var brand_id:Int?,
    var sales_price:Double?,
    var product_image:String?,
    var client_id:Int?,
    var order_case_qty:Int?,
    var order_unit_qty:Int?,
    var case_qty:Int? = 0,
    var unit_qty:Int? = 0,
)
