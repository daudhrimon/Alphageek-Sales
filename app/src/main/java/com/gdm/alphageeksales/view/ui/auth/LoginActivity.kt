package com.gdm.alphageeksales.view.ui.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.remote.LocationAddress
import com.gdm.alphageeksales.databinding.ActivityLoginBinding
import com.gdm.alphageeksales.utils.*
import com.gdm.alphageeksales.utils.Utils.EMAIL_PATTERN
import com.gdm.alphageeksales.view.ui.update_profile.ProfileUpdateActivity
import com.gdm.alphageeksales.view.ui.welcome_screen.WelcomeActivity
import com.gdm.alphageeksales.viewmodels.AuthViewModel
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var locationRequest: LocationRequest
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private var loginAddress = ""
    private var dialog: Dialog? = null
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Utils.currentPage = "Outlets"
        SharedPref.init(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
        ProgressLoader.init(this)

        binding.signUpLayout.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            userLogin()
        }


        // observe the registration response
        authViewModel.loginResponse.observe(this) {
            ProgressLoader.dismiss()
            if (it.success) {
                SharedPref.write("JWT_TOKEN",it.data?.access_token)
                showSuccessToast(this,it.message)
                if (it.data?.user_status == 2) {
                    startActivity(Intent(this, ProfileUpdateActivity::class.java))
                } else if (it.data?.user_status == 1) {
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finishAffinity()
                }
            } else {
                showErrorToast(this,it.message)
            }
        }

        // handle error
        authViewModel.errorMessage.observe(this) {
            showErrorToast(this,it.toString())
        }

        authViewModel.loading.observe(this) {
            if (it) {
                ProgressLoader.show()
            } else {
                ProgressLoader.dismiss()
            }
        }

        // location enabler alert dialog
        dialog = AlertDialog.Builder(this, R.style.Calender_dialog_theme)
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

    @SuppressLint("HardwareIds")
    private fun userLogin() {
        val email = binding.userEmail.text.toString()
        val password = binding.userPassword.text.toString()
        when {
            email.isEmpty() -> {
                binding.userEmail.requestFocus()
                binding.userEmail.error = "Email"
            }
            !email.matches(EMAIL_PATTERN) -> {
                binding.userEmail.requestFocus()
                binding.userEmail.error = "Invalid Email"
            }
            password.isEmpty() -> {
                binding.userPassword.requestFocus()
                binding.userPassword.error = "Password"
            }
            password.length < 6 -> {
                binding.userPassword.requestFocus()
                binding.userPassword.error = "Password should be 6 digits"
            }
            else -> {
                if (Utils.checkForInternet(this)){
                    val androidId: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    ProgressLoader.show()
                    val address = loginAddress.replace("null","")
                    authViewModel.userLogin(email,password,androidId,when{address.isNotEmpty()->address else->"Unknown"})
                }else{
                    showInfoToast(this,"No Internet Connection")
                }
            }
        }
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

    private fun initLocationInfo() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 10000
            smallestDisplacement = 170f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(gps: LocationResult) { gps?:return
                if (gps.locations.isNotEmpty()) {
                    val locationAddress = LocationAddress()
                    locationAddress.getAddressFromLocation(
                        (gps.lastLocation?.latitude ?: 0.0),
                        (gps.lastLocation?.longitude ?: 0.0),
                        this@LoginActivity,
                        GeoCodeHandler()
                    )
                }
            }
        }
    }
    @SuppressLint("HandlerLeak")
    internal inner class GeoCodeHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            loginAddress = when (message.what) {
                1 -> { val bundle = message.data
                    bundle.getString("address")?:"Unknown"
                } else -> "Unknown"
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

    override fun onResume() {
        super.onResume()
        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }
}