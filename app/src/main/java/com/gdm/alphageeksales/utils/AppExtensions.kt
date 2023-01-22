package com.gdm.alphageeksales.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty

fun <T : ImageView> T.loadImage(url: String?, placeHolder: Int) = apply {
    Picasso.get()
        .load(url)
        .error(placeHolder)
        .into(this)
    }

fun View.isVISIBLE() = apply { visibility = View.VISIBLE }
fun View.isGONE() = apply { visibility = View.GONE }

fun showSuccessToast(context: Context, message: String, length: Int = Toasty.LENGTH_SHORT) = Toasty.success(context, message, length,true).show()
fun showInfoToast(context: Context, message: String, length: Int = Toasty.LENGTH_SHORT) = Toasty.info(context, message, length,true).show()
fun showErrorToast(context: Context, message: String, length: Int = Toasty.LENGTH_SHORT) = Toasty.error(context, message, length,true).show()