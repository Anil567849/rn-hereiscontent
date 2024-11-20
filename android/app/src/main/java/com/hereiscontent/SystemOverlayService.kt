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
import android.widget.EditText
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Callback

class SystemOverlayService : Service() {
    private var overlayButton: View? = null
    private lateinit var windowManager: WindowManager

    companion object {
        var reactContext: ReactApplicationContext? = null
        var cb: Callback? = null;
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlayButton()
        // return START_STICKY
    }

    private fun showOverlayButton(){

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
            showForm(windowManager)
        }
    }

    private fun showForm(windowManager: WindowManager) {
        // Inflate the form layout
        val formView = LayoutInflater.from(this).inflate(R.layout.form_overlay, null)

        // Set up LayoutParams for the form
        val formParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

         // Remove FLAG_NOT_FOCUSABLE to make the form focusable
        formParams.flags = formParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()

        // Add the form to the window
        windowManager.addView(formView, formParams)

        // Get references to form fields
        val editText = formView.findViewById<EditText>(R.id.enterText)
        val submitButton = formView.findViewById<Button>(R.id.submitButton)

        // Handle form submission
        submitButton.setOnClickListener {
            val enteredText = editText.text.toString()
            if (enteredText.isNotEmpty()) {
                // Save the data to Notion (you can integrate this step later)
                Toast.makeText(applicationContext, "Data submitted: $enteredText", Toast.   LENGTH_SHORT).show()
                
                reactContext?.let { context ->
                    val systemOverlayModule = SystemOverlayModule(context)
                    systemOverlayModule.sendDataToReactNative("onFormSubmit", enteredText)
                }

                windowManager.removeView(formView)  // Remove the form after submission
            } else {
                Toast.makeText(applicationContext, "Please enter some data", Toast.LENGTH_SHORT).show()
            }
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