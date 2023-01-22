package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkout")
data class Checkout (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val checkout_id: Int?,
    val checkout_type: Int?,
    val checkout_date: String?,
    val is_confirm: Int?,
    val checkout_product: List<CheckoutProducts?>?
)