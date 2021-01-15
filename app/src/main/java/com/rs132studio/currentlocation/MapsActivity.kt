package com.rs132studio.currentlocation

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val FINE_LOCATION_RQ = 101
    private lateinit var mMap: GoogleMap
    private lateinit var mFusedClientLocationProvider: FusedLocationProviderClient
    private lateinit var location : Location
    private lateinit var marker : Marker
    private lateinit var latLng: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mFusedClientLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        checkForPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, "Location", FINE_LOCATION_RQ)


    }

    private fun checkForPermission(permission : String, name : String, requestCode : Int) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(this, permission) == (PackageManager.PERMISSION_GRANTED) -> {
                    val task = mFusedClientLocationProvider.lastLocation
                    task.addOnSuccessListener { location->
                        this.location = location
                        if (location != null){
                            val mapFragment = supportFragmentManager
                                    .findFragmentById(R.id.map) as SupportMapFragment
                            mapFragment.getMapAsync(this)
                        }

                    }

                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        fun innercheck(name : String){
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "$name permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }else{
                Toast.makeText(this, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        when(requestCode) {
            FINE_LOCATION_RQ -> innercheck("Location")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Permission Required")
            setMessage("Permission is access your $name is required to use this app")
            setPositiveButton("OK") {dialog, which ->
                ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(permission), requestCode)
            }
        }

        val dialog = builder.create()
        dialog.show()


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        latLng = LatLng(location.latitude, location.longitude)
        drawMarker(latLng)
    }

    private fun drawMarker(latLng: LatLng) {
        var markerOptions = MarkerOptions().position(latLng).title("You are at").snippet(getAddress(latLng.latitude, latLng.longitude))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        mMap.addMarker(markerOptions)
        marker.showInfoWindow()


    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        val address = geocoder.getFromLocation(latitude, longitude, 1)
        return address[0].getAddressLine(0).toString()

    }
}