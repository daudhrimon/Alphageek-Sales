package com.gdm.alphageeksales.data.remote.document_type

data class DocumentType(
    val id: Int,
    val name: String
){
    override fun toString(): String {
        return name
    }
}