package com.yourcompany.geofencing

import android.Manifest
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import nl.wemamobile.wemalibrary.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yourcompany.geofencing.databinding.ActivityMapsBinding


const val GEOFENCE_RADIUS = 200
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
const val GEOFENCE_LOCATION_REQUEST_CODE = 12345
const val LOCATION_REQUEST_CODE = 123

@SuppressLint("UnspecifiedImmutableFlag")
class MapsActivity : ApplicationActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private lateinit var map: GoogleMap
    private lateinit var geofencingClient : GeofencingClient
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceReceiver::class.java)
        intent.action = "MapsActivity.geofencing.action.ACTION_GEOFENCE_EVENT"
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        map.setOnMapLongClickListener(this)

        checkLocationPersmissions {
            if (it) {
                // Heeft toestemming
                println("toestemming")
                map.isMyLocationEnabled = true
            } else {
                println("geen toestemming")
                // Heeft geen toestemming
            }
        }
    }



    fun checkLocationPersmissions(completionHandler: (permissionsGranted: Boolean) -> Unit){
        if(ActivityCompat.checkSelfPermission(globalContext, ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(globalContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(globalContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < 29) {

                if( ActivityCompat.checkSelfPermission(globalContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(globalContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
                    // Android 9 and lower
                    DialogHelper.showOkDialog(globalContext.getString(R.string.location_dialog_text_android_10), DialogInterface.OnClickListener { dialog, which ->
                        //GEOFENCE
                        checkPersmissions(
                            listOf(ACCESS_FINE_LOCATION,
                                ACCESS_COARSE_LOCATION)) {
                            completionHandler(it)
                        }
                    })
                } else {
                    completionHandler(true)
                }
            } else if (Build.VERSION.SDK_INT == 29) {
                // Android 10
                DialogHelper.showOkDialog(globalContext.getString(R.string.location_dialog_text_android_10), DialogInterface.OnClickListener { dialog, which ->
                    //GEOFENCE
                    checkPersmissions(listOf(ACCESS_BACKGROUND_LOCATION,
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION)) {
                        completionHandler(it)
                    }
                })
            } else {
                // Android 11 and above
                DialogHelper.showOkDialog(globalContext.getString(R.string.location_dialog_text_android_10), DialogInterface.OnClickListener { dialog, which ->
                    //GEOFENCE
                    checkPersmissions(listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
                        if(it) {
                            DialogHelper.showOkDialog(globalContext.getString(R.string.location_dialog_text_android_11), DialogInterface.OnClickListener { dialog, which ->
                                //GEOFENCE
                                checkPersmissions(listOf(ACCESS_BACKGROUND_LOCATION)) {
                                    completionHandler(it)
                                }
                            })
                        } else {
                            completionHandler(it)
                        }
                    }
                })
            }
        } else {
            completionHandler(true)
        }
    }
    

    override fun onMapLongClick(p0: LatLng) {
        addMarker(p0)
        addCircle(p0, GEOFENCE_RADIUS.toDouble())
//        createGeoFence(p0, "key", geofencingClient)
    }

    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng)
        map.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng, radius : Double) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius)
        circleOptions.strokeColor(Color.argb(50, 70, 70, 70))
        circleOptions.fillColor(Color.argb(70, 150, 150, 150))
        circleOptions.strokeWidth(4F)
        map.addCircle(circleOptions)
    }

//    @SuppressLint("MissingPermission")
//    private fun createGeoFence(location: LatLng, key: String, geofencingClient: GeofencingClient) {
//        val geofence = Geofence.Builder()
//            .setRequestId(GEOFENCE_ID)
//            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
//
//            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
//            .build()
//
//        val geofenceRequest = GeofencingRequest.Builder()
//            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//            .addGeofence(geofence)
//            .build()
//
//
//        geofencingClient.removeGeofences(geofencePendingIntent).run {
//            addOnCompleteListener {
//                geofencingClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
//                    addOnSuccessListener {
//                        //                        Toast.makeText(this, R.string.geofences_added,
//                        //                            Toast.LENGTH_SHORT)
//                        //                            .show()
//                        println("geofence geactiveerd")
//                    }
//                    addOnFailureListener {
//                        //                        Toast.makeText(this, R.string.geofences_not_added, Toast.LENGTH_SHORT).show()
//                        if ((it.message != null)) {
//                            println(it.message)
//                        }
//                    }
//                }
//            }
//        }
//    }

//    private fun isLocationPermissionGranted() : Boolean {
//        return ContextCompat.checkSelfPermission(
//            this, ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
//            applicationContext, ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == GEOFENCE_LOCATION_REQUEST_CODE) {
//            if (permissions.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(
//                    this,
//                    "This application needs background location to work on Android 10 and higher",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//        if (requestCode == LOCATION_REQUEST_CODE) {
//            if (
//                grantResults.isNotEmpty() && (
//                        grantResults[0] == PackageManager.PERMISSION_GRANTED ||
//                                grantResults[1] == PackageManager.PERMISSION_GRANTED)
//            ) {
//                if (ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    return
//                }
//                map.isMyLocationEnabled = true
//                onMapReady(map)
//            } else {
//                Toast.makeText(
//                    this,
//                    "The app needs location permission to function",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                if (grantResults.isNotEmpty() && grantResults[2] != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(
//                        this,
//                        "This application needs background location to work on Android 10 and higher",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }

}