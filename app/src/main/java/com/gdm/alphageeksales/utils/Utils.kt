package com.gdm.alphageeksales.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import com.gdm.alphageeksales.data.local.down_sync.Brief
import com.gdm.alphageeksales.data.local.down_sync.Inventory
import com.gdm.alphageeksales.data.local.down_sync.Schedule
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.net.*
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object Utils {
    lateinit var currentModule: String
    lateinit var currentPage: String
    lateinit var currentSchedule: Schedule
    lateinit var currentBrief: Brief
    var haveToSync: Boolean = false

    var username :String = ""
    var gio_lat: String? = null
    var gio_long: String? = null
    var startTime = "0"
    var endTime = "0"

    var orderIdDelivery :Long = 0
    var orderIdGlobal: Long = 0
    var inventoryChangeList = ArrayList<Inventory>()

    val EMAIL_PATTERN: Regex = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")

    fun getMobileIPAddress(): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (into in interfaces) {
                val addressList: List<InetAddress> = Collections.list(into.inetAddresses)
                for (address in addressList) {
                    if (!address.isLoopbackAddress) {
                        return address.hostAddress
                    }
                }
            }
        } catch (_: Exception) {} // for now eat exceptions
        return ""
    }

    fun getWifiIPAddress(context: Context): String {
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val linkAddresses = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)?.linkAddresses
        val ipV4Address = linkAddresses?.firstOrNull { linkAddress -> linkAddress.address.hostAddress?.contains('.') ?: false }?.address?.hostAddress
        return ipV4Address.toString()
    }

    fun convertMultiPart(value: String): RequestBody {
        return RequestBody.create(MediaType.parse("multipart/form-data"), value)
    }

    fun convertImageMultiPart(imageUri: Uri, name: String): MultipartBody.Part {
        val file = File(imageUri.path!!)
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)
        return MultipartBody.Part.createFormData(name, file.name, requestFile)
    }

    // convert image to base64
    fun convertImageToBase64(contentResolver: ContentResolver, uri: Uri): String {
        try {
            val file = uri.path?.let { File(it) }
            val bitmap = getBitmap(contentResolver, uri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val resizedBitmap = bitmap?.let { Bitmap.createScaledBitmap(it,bitmap.width / 2,bitmap.height / 2,false) }
            resizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            val imageData = "data:image/${file?.extension?:"jpeg"};base64,"+Base64.encodeToString(byteArray, Base64.DEFAULT)
            Log.wtf("image_data",imageData)
            return imageData
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun getBitmap(contentResolver: ContentResolver, fileUri: Uri?): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, fileUri!!))
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
            }
        } catch (_: Exception) { null }
    }

    @SuppressLint("MissingPermission")
    fun checkForInternet(context: Context): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // if the android version is M or above
                val network = connectivityManager.activeNetwork ?: return false
                // Representation of the capabilities of an active network.
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
                return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            }
            else -> {
                // if the android version is below M
                @Suppress("DEPRECATION") val networkInfo = connectivityManager.activeNetworkInfo ?: return false
                @Suppress("DEPRECATION") return networkInfo.isConnected && networkInfo.isAvailable
            }
        }
    }

    fun getDistance(lat1: Double?, long1: Double?, lat2: Double?, long2: Double?): Double {
        val theta = (long1?:0.0)-(long2?:0.0)
        var dist = (sin(deg2rad(lat1)) * sin(deg2rad(lat2)) +(cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515 // miles
        dist *= 1609.344 // meter
        return (dist.toString().replace("-","")).toDouble()
    }
    private fun deg2rad(deg: Double?): Double = (deg?:0.0) * Math.PI / 180.0
    private fun rad2deg(rad: Double?): Double = (rad?:0.0) * 180.0 / Math.PI
}