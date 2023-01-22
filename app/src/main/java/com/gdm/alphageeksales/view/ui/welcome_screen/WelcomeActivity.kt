package com.gdm.alphageeksales.view.ui.welcome_screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.gdm.alphageeksales.MainActivity
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.local.down_sync.Outlet
import com.gdm.alphageeksales.data.local.down_sync.Schedule
import com.gdm.alphageeksales.data.local.image_model.ImageModel
import com.gdm.alphageeksales.data.local.sales_order_generation.SalesOrderData
import com.gdm.alphageeksales.data.local.visit.ScheduleVisit
import com.gdm.alphageeksales.data.remote.LocationAddress
import com.gdm.alphageeksales.data.remote.profile.ProfileData
import com.gdm.alphageeksales.databinding.ActivityWelcomeBinding
import com.gdm.alphageeksales.databinding.DialogUpsyncBinding
import com.gdm.alphageeksales.utils.*
import com.gdm.alphageeksales.view.ui.auth.LoginActivity
import com.gdm.alphageeksales.viewmodels.DataSyncViewModel
import com.gdm.alphageeksales.viewmodels.ProfileViewModel
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.RequestBody
import java.util.*

@AndroidEntryPoint
class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val upSyncForLogout = MutableLiveData<Boolean>()
    private var updatableOutletList = ArrayList<Outlet>()
    private var visitList = ArrayList<ScheduleVisit>()
    private var scheduleList = ArrayList<Schedule>()
    private var outletList = ArrayList<Outlet>()
    private lateinit var syncDialog: AlertDialog
    private var dialog: Dialog? = null
    private var locationAddress = ""
    private var doLogout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        SharedPref.init(this)
        initializeSyncDialog()
        setupProfileInfo()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        binding.salesAndOrderLayout.setOnClickListener {
            navigateToMainPage(Constants.MODULE_SALES_AND_ORDER, Constants.NAVIGATION_DASHBOARD)
        }
        binding.invoiceLayout.setOnClickListener {
            navigateToMainPage(Constants.MODULE_INVOICE, Constants.NAVIGATION_INVOICE_LIST)
        }
        binding.inventoryLayout.setOnClickListener {
            navigateToMainPage(Constants.MODULE_INVENTORY, Constants.NAVIGATION_INVENTORY)
        }
        binding.profileCard.setOnClickListener {
            ProfileDialog(this,upSyncForLogout).show()
        }

        // get user profile
        if (Utils.checkForInternet(this)){
            syncDialog.show()
            profileViewModel.getProfileInfo()
        } else {
            setupProfileInfo()
        }

        // observe profile response
        profileViewModel.profileResponse.observe(this) {
            try {
                if (it != null) {
                    if (it.success) {
                        // starting for the up sync process
                        dataSyncViewModel.getAllOfflineScheduleList()
                        SharedPref.write("PROFILE", Gson().toJson(it.data))
                        SharedPref.write("USER_ID", it.data.details?.user_id.toString())
                        SharedPref.write("PHONE",it.data.details?.phone.toString())
                        setupProfileInfo()
                    }
                }
            } catch (e: Exception) {
                setupProfileInfo()
                if (syncDialog.isShowing) { syncDialog.dismiss() }
                if (Utils.checkForInternet(this)) {
                    SharedPref.write("PROFILE", "")
                    SharedPref.write("USER_ID", "")
                    SharedPref.write("PHONE", "")
                    showErrorToast(this,"Please Login Again")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        // data upload sync
        dataSyncViewModel.upSyncResponse.observe(this) {
            if (it != null && it) {
                if (doLogout) {
                    doLogout = false
                    syncDialog.dismiss()
                    if (Utils.checkForInternet(this)) {
                        ProgressLoader.init(this)
                        showSuccessToast(this,"UpSync Successful, Please wait...logout process is running")
                        ProgressLoader.show()
                        val  address = locationAddress.replace("null","")
                        profileViewModel.userLogout(Utils.getWifiIPAddress(this),when{address.isNotEmpty()->address else->"Unknown"})
                    } else {
                        showInfoToast(this,"UpSync Successful, No Internet Connection...logout process skipped")
                    }
                } else {
                    dataSyncViewModel.downSync()
                }
            }
        }

        // data down sync
        dataSyncViewModel.downSyncResponse.observe(this) {
            if (it != null && it) {
                if (syncDialog.isShowing) {
                    syncDialog.dismiss()
                    showSuccessToast(this,"Database updated Successfully")
                }
            }
        }

        // observe profile related error
        profileViewModel.errorMessage.observe(this) {
            if (it != null) {
                showErrorToast(this,it.toString())
                if (syncDialog.isShowing) { syncDialog.dismiss() }
                doLogout = false
            }
        }

        // handle sync error
        dataSyncViewModel.errorMessage.observe(this) {
            doLogout = false
            if (it != null) {
                showErrorToast(this,it.toString())
                if (syncDialog.isShowing) { syncDialog.dismiss() }
            }
        }

        // sync button click
        binding.syncBtn.setOnClickListener {
            doLogout = false
            if (Utils.checkForInternet(this)) {
                syncDialog.show()
                profileViewModel.getProfileInfo()
            } else {
                showInfoToast(this,"No Internet Connection")
            }
        }

        // observe upSyncForLogout
        upSyncForLogout.observe(this) {
            if (Utils.checkForInternet(this)) {
                doLogout = true
                profileViewModel.getProfileInfo()
            } else {
                showInfoToast(this,"No Internet Connection")
            }
        }

        // observe logout response
        profileViewModel.logoutResponse.observe(this) {
            ProgressLoader.dismiss()
            if ((it?.message?:"").isNotEmpty()) {
                if (it.message == "Successfully logged out") {
                    showSuccessToast(this,it.message)
                    SharedPref.write("PROFILE", "")
                    SharedPref.write("USER_ID", "")
                    SharedPref.write("PHONE", "")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    showErrorToast(this,it.message)
                }
            }
        }

        // observe schedule data
        dataSyncViewModel.offlineScheduleList.observe(this) {
            if (it != null) {
                // set up the information
                scheduleList.clear()
                scheduleList.addAll(it)
                // get all outlet
                dataSyncViewModel.getAllOfflineOutlet()
            }
        }

        // observe outlet list data
        dataSyncViewModel.localOutletList.observe(this) {
            if (it != null) {
                // set up the information
                outletList.clear()
                outletList.addAll(it)
                dataSyncViewModel.getAllUpdatableOutlet()
            }
        }

        // observe updatable outletList
        dataSyncViewModel.updatableOutletList.observe(this) {
            if (it != null) {
                updatableOutletList.clear()
                updatableOutletList.addAll(it)
                dataSyncViewModel.getScheduleVisitData()
            }
        }

        // observe schedule visit data
        dataSyncViewModel.scheduleVisitList.observe(this) {
            if (it != null) {
                // set up the information
                visitList.clear()
                visitList.addAll(it)
                processAllData()
            }
        }

        // location enabler alert dialog
        dialog = AlertDialog.Builder(this,R.style.Calender_dialog_theme)
            .setTitle("Enable GPS/Location")
            .setMessage("Go to settings & enable GPS/Location access")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent) }
            .setCancelable(false).create()

        // register Gps BrodCast Receiver
        registerReceiver(GpsBrodCastReceiver(dialog), IntentFilter(LocationManager.MODE_CHANGED_ACTION))

        // get Locations
        initLocationInfo()
    }

    private fun showGpsEnablerDialogs() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isGpsEnabled || isNetworkEnabled) {
            dialog?.dismiss()
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            startLocationUpdates()
        } else {
            dialog?.show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Utils.checkForInternet(this)){
            showGpsEnablerDialogs()
        } else {
            Utils.gio_lat = null
            Utils.gio_long = null
        }
        if (Utils.haveToSync) {
            Utils.haveToSync = false
            doLogout = false
            syncDialog.show()
            profileViewModel.getProfileInfo()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Utils.checkForInternet(this)){
            stopLocationUpdates()
        }
    }

    private fun initializeSyncDialog() {
        val builder = AlertDialog.Builder(this,R.style.Calender_dialog_theme)
        val customView = DialogUpsyncBinding.inflate(layoutInflater)
        builder.setView(customView.root)
        syncDialog = builder.create()
        syncDialog.setCancelable(false)
        syncDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setupProfileInfo() {
        val profileInfo = Gson().fromJson(SharedPref.getUserInfo()?:"",ProfileData::class.java)
        profileInfo?.details?.image?.let {
            when{it.isNotEmpty()-> Picasso.get().load(it).error(R.drawable.ic_user).into(binding.profileImg)}
        }
        profileInfo?.reg_info?.name?.let {it.replace("  "," ").also { name->
            binding.welcomeMsg.text = "Hello, $name"
            Utils.username = name
        }}
    }

    private fun navigateToMainPage(currentModule: String, currentPage: String) {
        Utils.currentModule = currentModule
        Utils.currentPage = currentPage
        startActivity(Intent(this, MainActivity::class.java))
    }


    private fun processAllData() {
        if (outletList.isEmpty() && scheduleList.isEmpty() && visitList.isEmpty() && updatableOutletList.isEmpty()) {
            dataSyncViewModel.deleteInfoLocalDb()
        } else {
            val gson = Gson()
            val outletElement = gson.toJsonTree(outletList, object : TypeToken<List<Outlet?>?>() {}.type)
            val updateOutletElement = gson.toJsonTree(updatableOutletList, object : TypeToken<List<Outlet?>?>() {}.type)
            val scheduleElement = gson.toJsonTree(scheduleList, object : TypeToken<List<Schedule?>?>() {}.type)

            val jsonObject = JsonObject()
            val jsonArray = JsonArray()

            val outletArray = outletElement.asJsonArray
            val updateOutletArray   = updateOutletElement.asJsonArray
            val scheduleArray = scheduleElement.asJsonArray

            jsonObject.add("create_outlet", outletArray)
            jsonObject.add("update_outlet", updateOutletArray)
            jsonObject.add("create_schedule", scheduleArray)
            jsonObject.add("visit_data", jsonArray)

            println(visitList.size)
            visitList.forEach {
                val visitObject = JsonObject()

                when (it.visit_type?:0) {
                    1 -> { // no_sale
                        val imageList = Gson().fromJson(it.image_list, Array<ImageModel>::class.java).asList()
                        val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<ImageModel>?>() {}.type)

                        val noSaleObject = JsonObject()
                        noSaleObject.addProperty("user_id", SharedPref.getUserID())
                        noSaleObject.addProperty("schedule_id", it.schedule_id)
                        noSaleObject.addProperty("pre_schedule_id", it.pre_schedule_id)
                        noSaleObject.addProperty("pre_order_id", it.pre_order_id)
                        noSaleObject.addProperty("schedule_type", it.schedule_type)
                        noSaleObject.addProperty("outlet_id", it.outlet_id)
                        noSaleObject.addProperty("country_id", it.country_id)
                        noSaleObject.addProperty("state_id", it.state_id)
                        noSaleObject.addProperty("region_id", it.region_id)
                        noSaleObject.addProperty("location_id", it.location_id)
                        noSaleObject.addProperty("visit_date", it.visit_date)
                        noSaleObject.addProperty("visit_time", it.visit_time)
                        noSaleObject.addProperty("outlet_type_id", it.outlet_type_id)
                        noSaleObject.addProperty("outlet_channel_id", it.outlet_channel_id)
                        noSaleObject.addProperty("note", it.notes)
                        noSaleObject.add("visited_images", imageListType.asJsonArray)

                        noSaleObject.addProperty("stat_time", it.stat_time)
                        noSaleObject.addProperty("end_time", it.end_time)
                        noSaleObject.addProperty("gio_lat", it.gio_lat)
                        noSaleObject.addProperty("gio_long", it.gio_long)
                        noSaleObject.addProperty("is_exception", it.is_exception)
                        noSaleObject.addProperty("visit_distance", it.visit_distance)
                        noSaleObject.addProperty("isInternetAvailable", it.isInternetAvailable)

                        visitObject.add("no_sale",noSaleObject)
                        val json = gson.toJsonTree(visitObject, object : TypeToken<JsonObject>() {}.type)
                        jsonArray.add(json)
                        jsonObject.add("visit_data", jsonArray)
                    }
                    else -> { // ready_stock sale, order generation, order product delivery
                        if(it.sales_order_list != null && it.sales_order_list.isNotEmpty()) {
                            val salesOrderList = Gson().fromJson(it.sales_order_list, Array<SalesOrderData>::class.java).asList()
                            val salesOrderTree = Gson().toJsonTree(salesOrderList, object : TypeToken<List<SalesOrderData>?>() {}.type)
                            val imageList = Gson().fromJson(it.image_list, Array<ImageModel>::class.java).asList()
                            val imageListType = Gson().toJsonTree(imageList, object : TypeToken<List<ImageModel>?>() {}.type)

                            val visitOrderObject = JsonObject()
                            visitOrderObject.addProperty("order_id", it.order_id)
                            visitOrderObject.addProperty("grand_total", it.totalsale)
                            visitOrderObject.addProperty("paid_amount", it.paid_amount)
                            when{(it.due_amount?:"").isNotEmpty()->visitOrderObject.addProperty("due_amount",it.due_amount)}
                            visitOrderObject.addProperty("order_date", it.visit_date)
                            visitOrderObject.addProperty("order_time", it.visit_time)
                            visitOrderObject.addProperty("order_type", it.order_type)
                            visitOrderObject.addProperty("payment_type", it.payment_type)
                            visitOrderObject.add("order_list", salesOrderTree.asJsonArray)

                            val tempVisitObject = JsonObject()
                            tempVisitObject.addProperty("user_id", SharedPref.getUserID())
                            tempVisitObject.addProperty("schedule_id", it.schedule_id)
                            tempVisitObject.addProperty("pre_schedule_id", it.pre_schedule_id)
                            tempVisitObject.addProperty("pre_order_id", it.pre_order_id)
                            tempVisitObject.addProperty("schedule_type", it.schedule_type)
                            tempVisitObject.addProperty("outlet_id", it.outlet_id)
                            tempVisitObject.addProperty("country_id", it.country_id)
                            tempVisitObject.addProperty("state_id", it.state_id)
                            tempVisitObject.addProperty("region_id", it.region_id)
                            tempVisitObject.addProperty("location_id", it.location_id)
                            tempVisitObject.addProperty("visit_date", it.visit_date)
                            tempVisitObject.addProperty("visit_time", it.visit_time)
                            tempVisitObject.addProperty("outlet_type_id", it.outlet_type_id)
                            tempVisitObject.addProperty("outlet_channel_id", it.outlet_channel_id)

                            tempVisitObject.addProperty("stat_time", it.stat_time)
                            tempVisitObject.addProperty("end_time", it.end_time)
                            tempVisitObject.addProperty("gio_lat", it.gio_lat)
                            tempVisitObject.addProperty("gio_long", it.gio_long)
                            tempVisitObject.addProperty("is_exception", it.is_exception)
                            tempVisitObject.addProperty("visit_distance", it.visit_distance)
                            tempVisitObject.addProperty("isInternetAvailable", it.isInternetAvailable)

                            tempVisitObject.add("order", visitOrderObject)
                            tempVisitObject.addProperty("note", it.notes)
                            tempVisitObject.add("visited_images", imageListType.asJsonArray)

                            visitObject.add(when(it.visit_type?:0){2->"ready_stock" 3->"order_generate" 4->"delivery" else->""},tempVisitObject)
                            val json = gson.toJsonTree(visitObject, object : TypeToken<JsonObject>() {}.type)
                            jsonArray.add(json)
                            jsonObject.add("visit_data", jsonArray)
                        }
                    }
                }
            }.also {
                val bodyRequest: RequestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString())
                dataSyncViewModel.upSync(bodyRequest)
                Log.wtf("up_sync", jsonObject.toString())
            }
        }
    }

    //start location updates
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun initLocationInfo() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 20000
            fastestInterval = 20000
            smallestDisplacement = 170f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                if (p0.locations.isNotEmpty()) {
                    // use your location object
                    // get latitude , longitude and other info from this
                    Utils.gio_lat = (p0?.lastLocation?.latitude?:0.0).toString()
                    Utils.gio_long = (p0?.lastLocation?.longitude?:0.0).toString()
                    val locationAddress = LocationAddress()
                    locationAddress.getAddressFromLocation((p0.lastLocation?.latitude?:0.0),(p0.lastLocation?.longitude?:0.0),this@WelcomeActivity,GeoCodeHandler())
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    internal inner class GeoCodeHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            locationAddress = when (message.what) {
                1 -> { val bundle = message.data
                    bundle.getString("address")?:"Unknown"
                } else -> "Unknown"
            }
            try {
                val address = locationAddress.replace("null","")
                dataSyncViewModel.userLocation(Utils.gio_lat ?: "0.0", Utils.gio_long ?: "0.0",when{address.isNotEmpty()->address else->"Unknown"})
            } catch (_ :Exception){}
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        showGpsEnablerDialogs()
                    }
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                return
            }
        }
    }
}