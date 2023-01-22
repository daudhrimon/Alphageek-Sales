package com.gdm.alphageeksales.database

import androidx.room.TypeConverter
import com.gdm.alphageeksales.data.local.down_sync.Answers
import com.gdm.alphageeksales.data.local.down_sync.CheckoutProducts
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun checkoutProductsToJson(list: List<CheckoutProducts?>?): String? = Gson().toJson(list)

    @TypeConverter
    fun jsonToCheckoutProducts(json: String?): List<CheckoutProducts?>? = Gson().fromJson(json, object : TypeToken<List<CheckoutProducts?>?>() {}.type)

    @TypeConverter
    fun answersToJson(list: List<Answers?>?): String? = Gson().toJson(list)

    @TypeConverter
    fun jsonToAnswers(json: String?): List<Answers?>? = Gson().fromJson(json, object : TypeToken<List<Answers?>?>() {}.type)
}