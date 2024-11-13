package nl.ovfietsbeschikbaarheid.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dev.jordond.compass.Coordinates
import kotlinx.coroutines.tasks.await

class LocationLoader(
    private val context: Context
){
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    suspend fun loadCurrentCoordinates(): Coordinates? {
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

private fun Location.toCoordinates() = Coordinates(latitude, longitude)