package com.yourcompany.geofencing

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}

/**
 * Stores latitude and longitude information along with a hint to help user find the location.
 */
data class LandmarkDataObject(val id: String, val hint: Int, val name: Int, val latLong: LatLng)

internal object GeofencingConstants {
    // The geofences data
    val LANDMARK_DATA = arrayOf(
//        LandmarkDataObject(
//            "Wildlands",
//            R.string.golden_gate_bridge_hint,
//            R.string.golden_gate_bridge_location,
//            LatLng(52.78300864486902, 6.8911734117208745)),

        LandmarkDataObject(
            "WeMa Mobile",
            R.string.ferry_building_hint,
            R.string.ferry_building_location,
            LatLng(52.574773582748406, 6.595911842399657)),
//
//        LandmarkDataObject(
//            "Kuipershof",
//            R.string.ferry_building_hint,
//            R.string.ferry_building_location,
//            LatLng(52.746423294108745, 6.967251298227053)),
//
//        LandmarkDataObject(
//            "N34",
//            R.string.ferry_building_hint,
//            R.string.ferry_building_location,
//            LatLng(   52.59063733658867, 6.632868455892675))
    )
    const val GEOFENCE_RADIUS_IN_METERS = 200f
}