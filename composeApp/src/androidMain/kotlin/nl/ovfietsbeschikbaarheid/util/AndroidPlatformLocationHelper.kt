package nl.ovfietsbeschikbaarheid.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

class AndroidPlatformLocationHelper(private val context: Context): PlatformLocationHelper {

    private lateinit var _activity: WeakReference<ComponentActivity>
    fun setActivity(activity: ComponentActivity) {
        this._activity = WeakReference(activity)
    }

    override fun isGpsTurnedOn(): Boolean {
        // Can be replaced with geolocator.isAvailable(), but this would cause an extra dependency for just 2 lines of code
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun turnOnGps() {
        _activity.get()?.apply {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    override fun shouldShowLocationRationale(): Boolean {
        return _activity.get()?.let {
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
            shouldShow
        } ?: false
    }

    // On Android, we just can't know whether it's denied permanently, so return false, and we'll find out when the user presses the button
    override fun isDeniedPermanently(): Boolean = false
}