package nl.ovfietsbeschikbaarheid.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import dev.jordond.compass.Priority
import dev.jordond.compass.permissions.LocationPermissionController
import dev.jordond.compass.permissions.PermissionState
import dev.jordond.compass.permissions.mobile
import dev.jordond.compass.permissions.mobile.openSettings
import java.lang.ref.WeakReference

class LocationPermissionHelper(
    private val context: Context
){
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

    fun isGpsTurnedOn(): Boolean {
        // Can be replaced with geolocator.isAvailable(), but this would cause an extra dependency for just 2 lines of code
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun turnOnGps() {
        _activity.get()?.apply {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    fun hasGpsPermission() = LocationPermissionController.mobile().hasPermission()

    fun openSettings() = LocationPermissionController.openSettings()

    suspend fun requirePermission(): PermissionState = LocationPermissionController.mobile().requirePermissionFor(Priority.Balanced)
}