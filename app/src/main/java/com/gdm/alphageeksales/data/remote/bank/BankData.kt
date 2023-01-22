package com.gdm.alphageeksales.data.remote.bank

data class BankData(
    val bank_name: String,
    val id: Int
){
    override fun toString(): String {
        return bank_name
    }
}