package com.family.guardian.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import android.view.Gravity

class BlockedAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra("blocked_package") ?: ""

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        layout.addView(TextView(this).apply {
            text = "This app is limited right now"
            textSize = 20f
            gravity = Gravity.CENTER
        })
        layout.addView(TextView(this).apply {
            text = "Ask a parent if you need access to $pkg."
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        })
        setContentView(layout)
    }
}
