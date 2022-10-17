package com.yourcompany.geofencing

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import nl.wemamobile.wemalibrary.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yourcompany.geofencing.databinding.ActivityMapsBinding

class MapsActivity : ApplicationActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var geofencingClient : GeofencingClient
    private lateinit var binding: ActivityMapsBinding

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceReceiver::class.java)
        val flags = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> FLAG_UPDATE_CURRENT or FLAG_MUTABLE
            else -> FLAG_UPDATE_CURRENT
        }
        getBroadcast(this, 0, intent, flags)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val wema = LatLng(52.57519741307314, 6.595944030687099)
        map.addMarker(MarkerOptions().position(wema).title("Marker at WeMa"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(wema, 15F))

        checkLocationPersmissions {
            if (it) {
                map.isMyLocationEnabled = true
                addGeofences()
            } else {
                println("No permission")
            }
        }
    }

    private fun addGeofences() {
        GeofencingConstants.LANDMARK_DATA.forEach {
            addGeofence(it)
        }
    }

    private fun checkLocationPersmissions(completionHandler: (permissionsGranted: Boolean) -> Unit){
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

    private fun addCircle(latLng: LatLng, radius : Double) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius)
        circleOptions.strokeColor(Color.argb(50, 70, 0, 0))
        circleOptions.fillColor(Color.argb(70, 150, 0, 0))
        circleOptions.strokeWidth(4F)
        map.addCircle(circleOptions)
    }


    @SuppressLint("MissingPermission")
    private fun addGeofence(data : LandmarkDataObject) {
        val geofence = Geofence.Builder()
            .setRequestId(data.id)
            .setCircularRegion(
                data.latLong.latitude,
                data.latLong.longitude,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        addCircle(data.latLong, geofence.radius.toDouble())

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent).run {
            addOnSuccessListener {
                println("Geofence " + data.id + " toegevoegd!")
            }
            addOnFailureListener {
                if (it.message != null) {
                    println("Geofence " + data.id + " NIET toegevoegd")
                } else {
                    println("it message is null")
                }
            }
        }
    }
}