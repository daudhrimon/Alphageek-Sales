package com.gdm.alphageeksales.data.remote

data class InventoryResponse(
    var success:Boolean,
    var code:Int,
    var message:String,
    var data:Any
)
