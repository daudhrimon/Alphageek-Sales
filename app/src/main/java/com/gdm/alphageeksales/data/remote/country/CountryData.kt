package com.gdm.alphageeksales.data.remote.country

data class CountryData(
    val country_name: String,
    val id: Int
){
    override fun toString(): String {
        return country_name
    }
}