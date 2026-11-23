package com.family.guardian.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.family.guardian.service.LocationUpdateService
import com.family.guardian.service.ScreenTimeWorker
import java.util.concurrent.TimeUnit

/**
 * Setup flow the child/parent runs together, once, on the child's phone:
 * 1. Enter the pairing code shown on the parent dashboard.
 * 2. Grant Usage Access (screen time) — opens system settings, a visible step.
 * 3. Grant location permission (foreground + background).
 * 4. Enable the Accessibility Service (app blocking) — also a visible system settings step;
 *    Android does not allow this to be granted programmatically.
 *
 * Screen-cast is NOT set up here — it is requested per-session from the parent side and
 * always needs a fresh on-device accept, handled by ScreenCastConsentActivity.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main) — pairing-code field + the buttons below.

        schedulePeriodicScreenTimeReporting()
    }

    fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun startLocationSharing() {
        startForegroundService(Intent(this, LocationUpdateService::class.java))
    }

    private fun schedulePeriodicScreenTimeReporting() {
        val request = PeriodicWorkRequestBuilder<ScreenTimeWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(request)
    }
}
