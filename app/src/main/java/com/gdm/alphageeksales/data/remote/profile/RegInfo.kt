package com.gdm.alphageeksales.data.remote.profile

data class RegInfo(
    val account: Account,
    val app_type: Int,
    val created_at: String,
    val deleted_at: Any,
    val device_id: Any,
    val email: String,
    val email_verified_at: Any,
    val id: Int,
    val ip_address: String,
    val login_address: Any,
    val login_count: Int,
    val login_status: Int,
    val logout_address: Any,
    val name: String,
    val status: Int,
    val updated_at: String,
    val user_type: Int
)