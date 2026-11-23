package com.family.guardian.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.family.guardian.R

/**
 * Streams the child's screen to the parent dashboard. Android itself shows a persistent
 * "app is sharing your screen" indicator the whole time MediaProjection is active — on top
 * of the notification this service posts — and there is no API to suppress or hide it.
 * A session only exists because ScreenCastConsentActivity already got a live accept from
 * the child for THIS specific request; there is no "always allow" or scheduled auto-start.
 */
class ScreenCastService : Service() {

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val channelId = "screen_cast"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>("data") ?: return START_NOT_STICKY

        startForeground(2, buildNotification())

        val mpm = getSystemService(MediaProjectionManager::class.java)
        projection = mpm.getMediaProjection(resultCode, data)
        projection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { stopSelf() }
        }, null)

        // TODO: create a VirtualDisplay backed by an ImageReader or Surface, encode frames
        // (e.g. MediaCodec H.264) and stream them to the parent dashboard via the existing
        // WebRTC/socket connection — left as a stub here since the transport layer depends
        // on the backend you stand up for the dashboard.

        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Screen sharing", NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Your screen is being shared live")
            .setContentText("Tap to stop sharing")
            .setSmallIcon(R.drawable.ic_cast)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        virtualDisplay?.release()
        projection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
