package com.hereiscontent

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.util.Log
import android.widget.Button
import android.widget.Toast

class SystemOverlayService : Service() {
    private var overlayButton: View? = null
    private lateinit var windowManager: WindowManager

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Inflate the overlay button
        val inflatedButton = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)
        Log.d("OverlayButton", "Is View Null? ${inflatedButton == null}")

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            x = 10
            y = 10
        }

        // Add the overlay to the window
        windowManager.addView(inflatedButton, params)

        // Store the reference
        overlayButton = inflatedButton

        // Set up the click listener for the button
        val button = inflatedButton.findViewById<Button>(R.id.overlay_button)
        button?.setOnClickListener {
            // Show a Toast when the overlay button is clicked
            Toast.makeText(applicationContext, "Overlay Button Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayButton?.let { view ->
            windowManager.removeView(view)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}