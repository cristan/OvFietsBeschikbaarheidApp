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
        // TODO: Can be replaced with geolocator.isAvailable() when this is no longer a suspend fun: https://github.com/jordond/compass/issues/101
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

    suspend fun requirePermission(): PermissionState = LocationPermissionController.mobile().requirePermissionFor(Priority.Balanced)
}