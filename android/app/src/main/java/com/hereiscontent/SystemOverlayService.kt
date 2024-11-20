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
import android.widget.Spinner
import android.widget.Toast
import android.widget.EditText
import com.facebook.react.bridge.ReactApplicationContext
import android.widget.ArrayAdapter
import android.widget.AdapterView

data class SubmittedData(
    val inputTitle: String,
    val inputUrl: String,
    val inputCategory: String,
    val inputDescription: String,
    val selectedPlatform: String
)

class SystemOverlayService : Service() {
    private var overlayButton: View? = null
    private lateinit var windowManager: WindowManager

    companion object {
        var reactContext: ReactApplicationContext? = null
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
        val input_title = formView.findViewById<EditText>(R.id.input_title)
        val input_url = formView.findViewById<EditText>(R.id.input_url)
        val input_category = formView.findViewById<EditText>(R.id.input_category)
        val input_description = formView.findViewById<EditText>(R.id.input_description)
        val platform_spinner = formView.findViewById<Spinner>(R.id.platform_spinner)

        // Define the options to display in the Spinner
        val platforms = arrayOf("YouTube", "Instagram", "LinkedIn", "Facebook", "X", "TikTok", "Reddit")

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, platforms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Set the adapter to the Spinner
        platform_spinner.adapter = adapter
        var selectedPlatform: String = ""

        platform_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedPlatform = parentView.getItemAtPosition(position).toString()
                Log.d("Selected Platform", selectedPlatform) // Log the selected platform to check the value
            }
        
            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Handle the case when nothing is selected, if needed
                selectedPlatform = ""  // or some default value, if required
            }
        }

        val submitButton = formView.findViewById<Button>(R.id.submit_button)

        // Handle form submission
        submitButton.setOnClickListener {
            val inputTitle = input_title.text.toString();
            val inputUrl = input_url.text.toString();
            val inputCategory = input_category.text.toString();
            val inputDescription = input_description.text.toString();
            
            if (inputTitle.isNotEmpty()) {
                // Save the data to Notion (you can integrate this step later)
                Toast.makeText(applicationContext, "Data submitted: $inputTitle", Toast.   LENGTH_SHORT).show()

                reactContext?.let { context ->
                    val systemOverlayModule = SystemOverlayModule(context)
                    systemOverlayModule.sendDataToReactNative("onFormSubmit", inputTitle, inputUrl, inputCategory, inputDescription, selectedPlatform)
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