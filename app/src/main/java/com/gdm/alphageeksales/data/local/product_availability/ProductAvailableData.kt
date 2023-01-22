package com.gdm.alphageeksales.data.local.product_availability

data class ProductAvailableData(
    val product_id: Int,
    val case_qnt: Int,
    val unit_qty: Int,
    val sales_price: Int,
    val product_name: String,
    val product_image: String,
    val availability_qty: Int,
)
