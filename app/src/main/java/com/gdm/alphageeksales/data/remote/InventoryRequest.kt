package com.gdm.alphageeksales.data.remote

data class InventoryRequest(
    val product_id: Int?,
    val client_id: Int?,
    val brand_id: Int?,
    val category_id: Int?,
    val sales_price: Int?,
    val case_qty: Int?,
    val unit_qty: Int?,
    val unit_price: Int?
)