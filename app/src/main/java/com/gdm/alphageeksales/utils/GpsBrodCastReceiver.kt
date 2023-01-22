package com.gdm.alphageeksales.utils

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager

class GpsBrodCastReceiver(private val dialog: Dialog?) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action != null) {
            if (intent.action!!.matches(LocationManager.MODE_CHANGED_ACTION.toRegex())) {
                val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGpsEnabled || isNetworkEnabled) {
                    dialog?.dismiss()
                } else {
                    dialog?.show()
                }
            }
        }
    }
}