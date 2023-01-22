package com.gdm.alphageeksales.data.local.sales_order_genaration

class SalesOrderGenarationData(
    val product_id: Int,
    val category_id: Int,
    val brand_id: Int,
    val client_id: Int,
    val product_name: String,
    val product_image: String,
    val availability_qty: Int,

    val unit:Int,
    val case:Int,
    val unitPrice:Int,
    val costPrice:Int,
    val totalSale:Int,
    val paidAmount:Int
)