package com.gdm.alphageeksales.data.local.down_sync

data class DownSyncData(
    val brand_list: List<Brand>?,
    val brifs: List<Brief>?,
    val app_setting: AppSetting?,
    val dashboard: Dashboard?,
    val outlet_channels: List<OutletChannel>?,
    val outlet_types: List<OutletType>?,
    val outlets: List<Outlet>?,
    val products: List<Product>?,
    val location: List<LocationDownSync>?,
    val schedules: List<Schedule>?,
    val posms_product_list: List<Posm>?,
    var checkout: List<Checkout>?,
    val inventory: List<Inventory>?,
    val orderList: List<OrderResponse>?,
    val route_plane: List<RoutePlanResponse>?,
    val payment_types: List<PaymentType>?,
    val questions: List<Questions>?
)