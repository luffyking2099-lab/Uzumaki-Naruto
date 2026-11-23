package com.family.guardian.ui

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import com.family.guardian.service.ScreenCastService

/**
 * Shown on the CHILD'S device every time a parent requests a live screen-cast session.
 * This is not optional UI — MediaProjection requires the system's own "Start recording or
 * casting?" dialog to be accepted fresh for each session; it cannot be pre-approved or
 * triggered silently from the backend. If the child dismisses it, no cast starts.
 */
class ScreenCastConsentActivity : Activity() {

    private val REQUEST_CODE = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mpm = getSystemService(MediaProjectionManager::class.java)
        startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val serviceIntent = Intent(this, ScreenCastService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            }
            startForegroundService(serviceIntent)
        }
        // Approved or declined, this screen's job is done either way.
        finish()
    }
}
