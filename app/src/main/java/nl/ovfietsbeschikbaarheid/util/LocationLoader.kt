package nl.ovfietsbeschikbaarheid.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dev.jordond.compass.Coordinates
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class LocationLoader(
    private val context: Context
){
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun loadCurrentCoordinates(): Coordinates? {
        val playServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (playServicesAvailable != ConnectionResult.SUCCESS) {
            val locationService = context.getSystemService(LOCATION_SERVICE) as LocationManager
            return locationService.awaitCurrentLocation()?.toCoordinates()
        }

        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setDurationMillis(5000)
            .setGranularity(Granularity.GRANULARITY_COARSE)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        val cancellation = CancellationTokenSource()
        val location: Location? = fusedLocationClient
            .getCurrentLocation(currentLocationRequest, cancellation.token)
            .await()

        if (location != null) {
            return location.toCoordinates()
        }
        // Fall back to last known location
        return fusedLocationClient.lastLocation.await()?.toCoordinates()
    }
}

@SuppressLint("MissingPermission")
private suspend fun LocationManager.awaitCurrentLocation(): Location? {
    return suspendCancellableCoroutine { continuation ->
        val provider = LocationManager.GPS_PROVIDER

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                continuation.resume(location)
            }

            override fun onProviderDisabled(provider: String) {
                continuation.resume(null) // Provider was disabled, return null
            }
        }

        @Suppress("DEPRECATION")
        requestSingleUpdate(provider, locationListener, null)

        continuation.invokeOnCancellation {
            removeUpdates(locationListener)
        }
    }
}

private fun Location.toCoordinates() = Coordinates(latitude, longitude)