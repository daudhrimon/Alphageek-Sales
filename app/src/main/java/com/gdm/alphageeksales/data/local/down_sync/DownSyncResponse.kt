package com.gdm.alphageeksales.data.local.down_sync

data class DownSyncResponse(
    val code: Int,
    val `data`: DownSyncData,
    val message: String,
    val success: Boolean
)