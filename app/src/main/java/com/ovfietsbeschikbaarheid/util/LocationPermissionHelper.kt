package com.ovfietsbeschikbaarheid.util

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

class LocationPermissionHelper {
    private lateinit var _activity: WeakReference<ComponentActivity>
    fun shouldShowLocationRationale(): Boolean {
        return _activity.get()?.let {
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
            shouldShow
        } ?: false
    }

    fun setActivity(activity: ComponentActivity) {
        this._activity = WeakReference(activity)
    }
}