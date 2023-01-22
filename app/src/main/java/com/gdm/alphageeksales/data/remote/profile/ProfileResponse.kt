package com.gdm.alphageeksales.data.remote.profile

data class ProfileResponse(
    val code: Int,
    val data: ProfileData,
    val message: String,
    val success: Boolean
)