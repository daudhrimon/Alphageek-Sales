package com.gdm.alphageeksales.data.remote.education

data class EducationResponse(
    val code: Int,
    val `data`: List<EducationData>,
    val message: String,
    val success: Boolean
)