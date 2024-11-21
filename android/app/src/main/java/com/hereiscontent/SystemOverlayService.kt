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
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import android.widget.EditText
import com.facebook.react.bridge.ReactApplicationContext
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.Context
import android.view.Gravity
import android.content.res.Resources
import android.content.ClipboardManager

class SystemOverlayService : Service() {
    private var overlayButton: View? = null
    private var formViewRef: View? = null
    private lateinit var windowManager: WindowManager

    companion object {
        var reactContext: ReactApplicationContext? = null
    }

    private val backgroundReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (intent?.action == "com.hereiscontent.APP_IS_DESTROYED") {
                    hideOverlayButton()
                }
            } catch (e: Exception) {
                Log.e("BroadcastReceiver", "Error in onReceive: ${e.message}", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Register the receiver for background/foreground state changes
        try {          
            val filter = IntentFilter("com.hereiscontent.APP_IS_DESTROYED")
            registerReceiver(backgroundReceiver, filter, Context.RECEIVER_EXPORTED)
            showOverlayButton()
        } catch (e: Exception) {
            Log.e("BroadcastReceiver", "Error while registering receiver: ${e.message}", e)
        }
    }

    private fun showOverlayButton(){

        // Inflate the overlay button
        val inflatedButton = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)

        val displayMetrics = Resources.getSystem().displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
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
            // Position the button at the center right
            x = screenWidth - width - 50  // 50 is a padding from the right edge
            y = (screenHeight - height) / 2  // Center vertically
            gravity = Gravity.TOP or Gravity.START  // Important for positioning
        }

        // Add the overlay to the window
        windowManager.addView(inflatedButton, params)

        // Store the reference
        overlayButton = inflatedButton

        // Set up the click listener for the button
        val button = inflatedButton.findViewById<Button>(R.id.overlay_button)

        button?.setOnClickListener {
            showForm(windowManager)
        }
    }

    private fun hideOverlayButton() {
        overlayButton?.let {
            windowManager.removeView(it) // Remove overlay button when app is destroyed or in background
            overlayButton = null // Clear reference
        }

        formViewRef?.let { view ->
            windowManager.removeView(view)
            formViewRef = null
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

         // Remove FLAG_NOT_FOCUSABLE to make the form focusable - So i can see keyboard
        formParams.flags = formParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()

        // Add the form to the window
        windowManager.addView(formView, formParams)
        formViewRef = formView

        val scrollView = formView.findViewById<ScrollView>(R.id.scrollView)
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val layoutParams = scrollView.layoutParams
        layoutParams.height = screenHeight / 2
        scrollView.layoutParams = layoutParams

        // Get references to form fields
        val input_title = formView.findViewById<EditText>(R.id.input_title)
        val input_url = formView.findViewById<EditText>(R.id.input_url)
        val input_tags = formView.findViewById<EditText>(R.id.input_tags)  // Tags
        val input_description = formView.findViewById<EditText>(R.id.input_description)
        val platform_spinner = formView.findViewById<Spinner>(R.id.platform_spinner)

        // Handle Paste Button 
        val pasteButton = formViewRef?.findViewById<ImageButton>(R.id.paste_button)

        pasteButton?.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                input_url.setText(clipData.getItemAt(0).text.toString())
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
        }

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
            }
        
            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Handle the case when nothing is selected, if needed
                selectedPlatform = ""  // or some default value, if required
            }
        }

        val submitButton = formView.findViewById<Button>(R.id.submit_button)
        val closeButton = formView.findViewById<Button>(R.id.close_button)

        // Handle form submission
        submitButton?.setOnClickListener {
            val inputTitle = input_title.text.toString();
            val inputUrl = input_url.text.toString();
            val inputTags = input_tags.text.toString(); // Tags
            val inputDescription = input_description.text.toString();
            
            if (inputUrl.isNotEmpty() && inputTags.isNotEmpty()) {
                // Save the data to Notion (you can integrate this step later)
                Toast.makeText(applicationContext, "Data submitted: $inputUrl", Toast.   LENGTH_SHORT).show()

                reactContext?.let { context ->
                    val systemOverlayModule = SystemOverlayModule(context)
                    systemOverlayModule.sendDataToReactNative("onFormSubmit", inputTitle, inputUrl, inputTags, inputDescription, selectedPlatform)
                }

                windowManager.removeView(formView)  // Remove the form after submission
            } else {
                Toast.makeText(applicationContext, "Please enter url and tags", Toast.LENGTH_SHORT).show()
            }
        }

        closeButton?.setOnClickListener {
            windowManager.removeView(formView)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayButton?.let { view ->
            windowManager.removeView(view)
        }
        formViewRef?.let { view ->
            windowManager.removeView(view)
        }
        unregisterReceiver(backgroundReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}