package com.gdm.alphageeksales.view.ui.module.sales_and_order

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gdm.alphageek.data.local.down_sync.RoutePlanDetails
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsFragment : Fragment() {
    private lateinit var binding : FragmentMapsBinding
    private var locationList: ArrayList<RoutePlanDetails>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapsBinding.inflate(layoutInflater)

        locationList = Gson().fromJson(arguments?.getString("location_list"),object : TypeToken<ArrayList<RoutePlanDetails>>(){}.type)
        Log.wtf("ABC",Gson().toJson(locationList))

        if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            } else {
                ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val callback = OnMapReadyCallback { googleMap ->
                for (i in locationList?:ArrayList(emptyList())) {
                    var latitude = 9.072264
                    var longitude = 7.491302
                    var location = "Abuja"
                    try {
                        if (i.gio_lat != null && i.gio_long != null && i.location_name != null) {
                            latitude = i.gio_lat.toDouble()
                            longitude = i.gio_long.toDouble()
                            location = i.location_name.toString()
                        }
                        val markerPosition = LatLng(latitude,longitude)
                        googleMap.addMarker(MarkerOptions().position(markerPosition).title(location))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 6.0f))
                    } catch (e: Exception) {/**/}
                }
                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.uiSettings.isZoomGesturesEnabled = true
                googleMap.uiSettings.isCompassEnabled = true
            }
            withContext(Dispatchers.Main) {
                val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync(callback)
            }
        }

        return binding.root
    }
}