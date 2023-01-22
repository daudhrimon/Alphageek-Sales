package com.gdm.alphageeksales.data.local.sales_order_generation

class SalesOrderData(
    val brand_id: Int?,
    val client_id: Int?,
    val product_name: String?,
    val product_image: String?,
    val product_id: Int?,
    val product_category_id: Int?,
    val unit_per_case: Int?,
    val unit_price: Double?,
    var case_qty: Int?,
    var unit_qty: Int?,
    var cost_price: Double?,
    var order_case_qty: Int?,
    var order_unit_qty: Int?
)