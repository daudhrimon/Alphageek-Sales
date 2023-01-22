package com.gdm.alphageeksales.data.local.down_sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Questions(
    @PrimaryKey
    val id: Long,
    val question: String?,
    val ans_type: String?,
    val is_required: Int?,
    val created_at: String?,
    val updated_at: String?,
    val ck_ans: String?,
    var ans: String? = null
)
