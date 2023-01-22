package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_setting")
data class AppSetting(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String,
    var email: String,
    var footer_text: String?,
    var copy_right: String?,
    var time_zone: String?,
    var app_logo: String?,
    var favicon: String?,
)
