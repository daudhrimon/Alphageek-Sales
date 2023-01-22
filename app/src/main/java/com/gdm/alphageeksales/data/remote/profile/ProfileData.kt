package com.gdm.alphageeksales.data.remote.profile

data class ProfileData(
    val account: Account,
    val details: Details?,
    val guarantor: Guarantor,
    val reg_info: RegInfo
)