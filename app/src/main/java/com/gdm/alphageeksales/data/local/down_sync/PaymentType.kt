package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_type")
data class PaymentType(
    @PrimaryKey
    val id: Long?,
    val payment_type_name: String?
){ override fun toString() = payment_type_name?:"Cash Payment" }

