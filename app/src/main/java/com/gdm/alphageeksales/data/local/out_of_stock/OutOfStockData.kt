package com.gdm.alphageeksales.data.local.out_of_stock

data class OutOfStockData(
    val product_id: Int,
    val category_id: Int,
    val brand_id: Int,
    val client_id: Int,
    val product_name: String,
    val product_image: String?,
    var is_checked: Boolean,
)
