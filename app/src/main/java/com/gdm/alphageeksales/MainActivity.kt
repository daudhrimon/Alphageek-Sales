package com.gdm.alphageeksales

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.Navigation
import com.gdm.alphageeksales.data.remote.LocationAddress
import com.gdm.alphageeksales.databinding.ActivityMainBinding
import com.gdm.alphageeksales.utils.*
import com.gdm.alphageeksales.viewmodels.DataSyncViewModel
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationRequest: LocationRequest
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    //end location
    private val dataSyncViewModel: DataSyncViewModel by viewModels()
    private lateinit var currentFragment: String
    private val navController by lazy { Navigation.findNavController(this, R.id.navHostFragment) }
    lateinit var titleName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // nav menu click event listener
        titleName = binding.toolbar.titleName
        val hView = binding.navView.getHeaderView(0)
        val dashboardLayout = hView.findViewById<LinearLayout>(R.id.dashboardLayout)
        val routePlanLayout = hView.findViewById<LinearLayout>(R.id.routePlanLayout)
        val outletListLayout = hView.findViewById<LinearLayout>(R.id.outlet_list_layout)
        val visitScheduleLayout = hView.findViewById<LinearLayout>(R.id.visitScheduleLayout)
        val inboxLayout = hView.findViewById<LinearLayout>(R.id.inboxLayout)
        val helpLayout = hView.findViewById<LinearLayout>(R.id.helpLayout)
        val syncLayout = hView.findViewById<LinearLayout>(R.id.syncLayout)

        when (Utils.currentModule) {
            Constants.MODULE_INVOICE -> navController.navigate(R.id.invoiceListFragment)
            Constants.MODULE_INVENTORY -> navController.navigate(R.id.inventoryMangeFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragment = destination.label.toString()
            titleName.text = currentFragment
            when (Utils.currentModule) {
                Constants.MODULE_SALES_AND_ORDER -> {
                    routePlanLayout.isVISIBLE()
                    outletListLayout.isVISIBLE()
                    visitScheduleLayout.isVISIBLE()
                    when(currentFragment) {
                        Constants.NAVIGATION_ROUTE_PLAN -> routePlanLayout.isGONE()
                        Constants.NAVIGATION_OUTLET_LIST -> outletListLayout.isGONE()
                        Constants.NAVIGATION_VISIT_SCHEDULE -> visitScheduleLayout.isGONE()
                    }
                }
                Constants.MODULE_INVOICE, Constants.MODULE_INVENTORY -> {
                    routePlanLayout.isGONE()
                    outletListLayout.isGONE()
                    visitScheduleLayout.isGONE()
                }
            }
            when(currentFragment){Constants.NAVIGATION_INBOX->inboxLayout.isGONE() else->inboxLayout.isVISIBLE()}
            when(currentFragment){Constants.NAVIGATION_HELP->helpLayout.isGONE() else->helpLayout.isVISIBLE()}
        }

        binding.toolbar.menuBtn.setOnClickListener {
            binding.drawerLayout.open()
        }
        // event click listener
        dashboardLayout.setOnClickListener{
            when(Utils.currentModule) {
                Constants.MODULE_SALES_AND_ORDER -> {
                    when (currentFragment) { Constants.NAVIGATION_DASHBOARD -> finish()
                    else -> {
                            binding.drawerLayout.close()
                            navController.navigate(R.id.dashboardFragment)
                        }
                    }
                }
                Constants.MODULE_INVOICE -> {
                    when (currentFragment) { Constants.NAVIGATION_INVOICE_LIST -> finish()
                        else -> {
                            binding.drawerLayout.close()
                            navController.navigate(R.id.invoiceListFragment)
                        }
                    }
                }
                Constants.MODULE_INVENTORY -> {
                    when (currentFragment) { Constants.NAVIGATION_INVENTORY -> finish()
                        else -> {
                            binding.drawerLayout.close()
                            navController.navigate(R.id.inventoryMangeFragment)
                        }
                    }
                } }
        }
        routePlanLayout.setOnClickListener {
            binding.drawerLayout.close()
            when { currentFragment != Constants.NAVIGATION_ROUTE_PLAN -> navController.navigate(R.id.routePlanFragment) }
        }
        outletListLayout.setOnClickListener {
            binding.drawerLayout.close()
            when { currentFragment != Constants.NAVIGATION_OUTLET_LIST -> navController.navigate(R.id.outletListFragment) }
        }
        visitScheduleLayout.setOnClickListener {
            binding.drawerLayout.close()
            when { currentFragment != Constants.NAVIGATION_VISIT_SCHEDULE -> navController.navigate(R.id.scheduleFragment) }
        }
        inboxLayout.setOnClickListener {
            binding.drawerLayout.close()
            when { currentFragment != Constants.NAVIGATION_INBOX -> navController.navigate(R.id.inboxFragment) }
        }
        helpLayout.setOnClickListener {
            binding.drawerLayout.close()
            if (currentFragment != "Help & Support"){
                navController.navigate(R.id.helpAndSupportFragment)
            }
        }
        syncLayout.setOnClickListener {
            when { Utils.checkForInternet(this) -> {
                Utils.haveToSync = true
                finish()
            } else -> showInfoToast(this,"No internet connection") }
        }

        initLocationInfo()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        if (Utils.checkForInternet(this)){
            startLocationUpdates()
        } else {
            Utils.gio_lat = null
            Utils.gio_long = null
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.close()
        } else {
            when (currentFragment) {
                Constants.NAVIGATION_DASHBOARD -> finish()
                Constants.NAVIGATION_INVOICE_LIST -> finish()
                Constants.NAVIGATION_INVENTORY -> finish()
                Constants.NAVIGATION_OUTLET_LIST -> navController.navigate(R.id.dashboardFragment)
                else -> super.onBackPressed()
            }
        }
    }

    private fun initLocationInfo() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 10000
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
                    locationAddress.getAddressFromLocation((p0.lastLocation?.latitude?:0.0),(p0.lastLocation?.longitude?:0.0),this@MainActivity,GeoCodeHandler())
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    internal inner class GeoCodeHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            val locationAddress: String = when (message.what) {
                1 -> {
                    val bundle = message.data
                    bundle.getString("address")?:"Unknown"
                } else -> "Unknown"
            }
            try {
                val address = locationAddress.replace("null","")
                dataSyncViewModel.userLocation(Utils.gio_lat ?: "0.0", Utils.gio_long ?: "0.0",when{address.isNotEmpty()->address else->"Unknown"})
            } catch (_ :Exception){}
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }
}