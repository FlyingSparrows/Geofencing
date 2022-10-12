package com.yourcompany.geofencing

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.yourcompany.geofencing.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private lateinit var map: GoogleMap
    private var permissionDenied = false
    private lateinit var geofencingClient : GeofencingClient
    private lateinit var binding: ActivityMapsBinding
    private val REQUEST_LOCATION_PERMISSION = 1
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    private val TAG = "HuntMainActivity"
    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        enableMyLocation()
    }

    private fun checkForegroundPermission() : Boolean {
        return ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkBackgroundPermission() : Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        return checkForegroundPermission() && checkBackgroundPermission()
    }

    @SuppressLint("InlinedApi", "MissingPermission")
    private fun enableMyLocation() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            map.isMyLocationEnabled = true
            return
        } else {
            var permissionArray = arrayOf(permission.ACCESS_FINE_LOCATION)
            val resultCode = when {
                runningQOrLater -> {
                    permissionArray += permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                }
                else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            }
            Log.d(TAG, "Request foreground only location permission")
            ActivityCompat.requestPermissions(
                this,
                permissionArray,
                resultCode
            )
        }
    }


//    @SuppressLint("MissingPermission", "InlinedApi")
//    private fun enableMyLocation()  {
//        if (!checkForegroundPermission()) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
//        } else {
//            if (!checkBackgroundPermission()) {
//                ActivityCompat.requestPermissions(
//                    this, arrayOf(permission.ACCESS_BACKGROUND_LOCATION),
//                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
//                )
//            } else {
//                map.isMyLocationEnabled = true
//            }
//        }
//    }

//
//    private fun isPermissionGranted() : Boolean {
//        return (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED
//                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED)
//    }


//    @SuppressLint("MissingPermission")
//    private fun enableMyLocation() {
//        // Check if permissions are granted, if so, enable the my location layer
//        if (checkPermissionsAreGranted()) {
//            map.isMyLocationEnabled = true
//        } else {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
//        }
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionResult")

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty() || grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED || (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE && grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED)) {
            enableMyLocation()
        //            Snackbar.make(
//                binding.root,
//                R.string.permission_denied_explanation,
//                Snackbar.LENGTH_INDEFINITE
//            )
//                .setAction(R.string.settings) {
//                    startActivity(Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    })
//                }.show()
        } else {
            println("hmm")
        }

//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
//                enableMyLocation()
//            }
//        }
    }
}