package com.gdm.alphageeksales.data.local.down_sync

class OrderResponse(
    var order_id: Long,
    var schedule_id: Long?,
    var outlet_id: Long?,
    var country_id: Int?,
    var region_id: Int?,
    var state_id: Int?,
    var location_id: Int?,
    var grand_total: Double?,
    var paid_amount: Double?,
    var due_amount: Double?,
    var change_amount: Double?,
    var payment_type: String?,
    var order_type: Long?,
    var order_date: String?,
    var image_url: String?,
    var outlet_name: String?,
    var user_name: String?,
    var outlet_phone: String?,
    val order_ditails: List<OrderDetails>
)