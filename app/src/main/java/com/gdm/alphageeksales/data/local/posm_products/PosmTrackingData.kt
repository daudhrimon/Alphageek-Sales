package com.gdm.alphageeksales.data.local.posm_products

data class PosmTrackingData(
    val product_id: Int,
    val category_id: Int,
    val brand_id: Int,
    val client_id: Int,
    val product_name: String,
    val product_image: String?,
    val availability_qty: Int,
    val status: String,
)
