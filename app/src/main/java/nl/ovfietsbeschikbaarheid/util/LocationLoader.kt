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
import timber.log.Timber
import kotlin.coroutines.resume

class LocationLoader(
    private val context: Context
){
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownCoordinates(): Coordinates? {
        val playServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (playServicesAvailable != ConnectionResult.SUCCESS) {
            return null
        }
        return fusedLocationClient.lastLocation.await()?.toCoordinates()
    }

    @SuppressLint("MissingPermission")
    suspend fun loadCurrentCoordinates(): Coordinates? {
        Timber.d("Loading location")
        val playServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (playServicesAvailable != ConnectionResult.SUCCESS) {
            // No Play Services. Fall back to the old fashioned way.
            val locationService = context.getSystemService(LOCATION_SERVICE) as LocationManager
            return locationService.awaitCurrentLocation()?.toCoordinates()
        }

        val currentLocationRequest = CurrentLocationRequest.Builder()
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
        // Fall back to last known location. We could use compass fully now, getting the last known location is now supported since version 2.2.
        return fusedLocationClient.lastLocation.await()?.toCoordinates()
    }
}

@SuppressLint("MissingPermission")
private suspend fun LocationManager.awaitCurrentLocation(): Location? {
    return suspendCancellableCoroutine { continuation ->
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                continuation.resume(location)
            }

            override fun onProviderDisabled(provider: String) {
                continuation.resume(null) // Provider was disabled, return null
            }
        }

        @Suppress("DEPRECATION")
        requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)

        continuation.invokeOnCancellation {
            removeUpdates(locationListener)
        }
    }
}

private fun Location.toCoordinates() = Coordinates(latitude, longitude)