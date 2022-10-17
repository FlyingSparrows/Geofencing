package com.yourcompany.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import nl.wemamobile.wemalibrary.DialogHelper.showOkDialog

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                println(errorMessage)
                return
            }

            val geofenceTransition = geofencingEvent.geofenceTransition

            println(geofenceTransition)

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                println(context.getString(R.string.geofence_entered))
               showOkDialog("Geofence gepasseerd!", DialogInterface.OnClickListener { dialog, which -> })
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                println("Je bent er nog steeds")
                showOkDialog("Nog steeds in de geofence", DialogInterface.OnClickListener { dialog, which -> })
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                println("Je bent uit de geofence gegaan")
                showOkDialog("Uit de geofence gegaan!", DialogInterface.OnClickListener { dialog, which -> })
            }

        }

    }
}