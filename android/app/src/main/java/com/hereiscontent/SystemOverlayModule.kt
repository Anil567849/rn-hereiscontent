package com.hereiscontent

import android.provider.Settings
import android.content.Intent
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.util.Log
import com.facebook.react.bridge.Callback
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson

data class SubmittedData(
    val inputTitle: String,
    val inputUrl: String,
    val inputTags: String,
    val inputDescription: String,
    val selectedPlatform: String
)

class SystemOverlayModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    init {
        SystemOverlayService.reactContext = reactContext
    }

    override fun getName(): String {
        return "SystemOverlayModule" // This name is used in JavaScript
    }

    @ReactMethod
    fun startOverlayService() {
        if (Settings.canDrawOverlays(reactContext)) {
            // Permission granted, start the service
            val serviceIntent = Intent(reactContext, SystemOverlayService::class.java)
            reactContext.startService(serviceIntent)
        } else {
            // Request permission
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            reactContext.startActivity(intent)
        }
    }

    @ReactMethod
    fun isOverlayPermissionGranted(promise: Promise) {
        try {
            val context = reactApplicationContext
            val isGranted = Settings.canDrawOverlays(context)
            promise.resolve(isGranted) // Returns true if permission is granted
        } catch (e: Exception) {
            promise.reject("ERROR_CHECKING_OVERLAY_PERMISSION", e)
        }
    }

    @ReactMethod
    fun sendDataToReactNative(eventName: String, inputTitle: String, inputUrl: String, inputTags: String, inputDescription: String, selectedPlatform: String) {
        val submittedData = SubmittedData(inputTitle, inputUrl, inputTags, inputDescription, selectedPlatform)

        val gson = Gson()
        val jsonData = gson.toJson(submittedData)

        reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(eventName, jsonData)
    }

}
