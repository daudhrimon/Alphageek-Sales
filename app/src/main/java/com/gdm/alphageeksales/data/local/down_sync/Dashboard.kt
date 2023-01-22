package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard")
data class Dashboard(
    @PrimaryKey
    var id:Int = 0,
    var outlets: Int?,
    val products: Int?,
    var sales_visit: Int?, // Visit Planed
    var visited_sales: Int?, // Actual Visited
    val login_count: Long?, // login count
    var daily_sales_amount: Double?, // daily ready stock sales amount
    var daily_generat_amount: Double? // daily order generation amount
)