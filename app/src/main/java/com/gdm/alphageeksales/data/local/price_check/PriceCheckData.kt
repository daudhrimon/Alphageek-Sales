package com.gdm.alphageeksales.data.local.price_check

data class PriceCheckData(
    val product_id: Int,
    val category_id: Int,
    val brand_id: Int,
    val client_id: Int,
    val product_name: String,
    val product_image: String,
    val unit_price: Double,
    val actual_price: Double?=0.0,
)
