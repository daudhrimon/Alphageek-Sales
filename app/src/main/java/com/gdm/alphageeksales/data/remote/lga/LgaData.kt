package com.gdm.alphageeksales.data.remote.lga

data class LgaData(
    val id: Int,
    val location_name: String
){
    override fun toString(): String {
        return location_name
    }
}