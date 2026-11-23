package com.family.guardian.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.family.guardian.R
import com.family.guardian.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Foreground service = Android keeps a notification icon visible in the status bar the
 * entire time location is being shared. This is intentional: it should never be possible
 * for location sharing to be invisible to the person carrying the phone.
 */
class LocationUpdateService : Service() {

    private lateinit var fused: FusedLocationProviderClient
    private val channelId = "location_sharing"

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification())

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5 * 60_000L)
            .setMinUpdateIntervalMillis(60_000L)
            .build()

        fused.requestLocationUpdates(request, callback, mainLooper)
        return START_STICKY
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            CoroutineScope(Dispatchers.IO).launch {
                runCatching {
                    ApiClient.service.reportLocation(
                        childId = ApiClient.childId(applicationContext),
                        lat = loc.latitude, lng = loc.longitude, accuracyM = loc.accuracy
                    )
                }
            }
        }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Location sharing", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location is being shared with your family")
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        fused.removeLocationUpdates(callback)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
