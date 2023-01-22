package com.gdm.alphageeksales.data.remote.bank

data class BankResponse(
    val code: Int,
    val `data`: List<BankData>,
    val message: String,
    val success: Boolean
)