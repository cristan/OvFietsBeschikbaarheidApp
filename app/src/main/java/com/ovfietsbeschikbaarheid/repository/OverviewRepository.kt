package com.ovfietsbeschikbaarheid.repository

import android.content.Context
import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.ext.distanceTo
import com.ovfietsbeschikbaarheid.mapper.LocationsMapper
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import dev.jordond.compass.Coordinates
import java.text.DecimalFormat
import kotlin.math.roundToInt

class OverviewRepository(private val context: Context) {
    private val client = KtorApiClient()

    private var allLocations = listOf<LocationOverviewModel>()

    // allLocations will be empty after app is recreated. This works around that, but there's probably a nicer way to do this.
    fun getAllLocations(): List<LocationOverviewModel> {
        if (allLocations.isEmpty()) {
            val response = client.getLocations(context)
            allLocations = LocationsMapper.map(response)
        }
        return allLocations
    }

    fun getLocationsWithDistance(currentCoordinates: Coordinates): List<LocationOverviewWithDistanceModel> {
        val kmFormat = DecimalFormat().apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }
        return getAllLocations()
            .sortedBy { it.distanceTo(currentCoordinates) }
            .map {
                val distance = it.distanceTo(currentCoordinates)
                val formattedDistance = if (distance < 1000) {
                    "${distance.roundToInt()} m"
                } else {
                    "${kmFormat.format(distance / 1000)} km"
                }
                LocationOverviewWithDistanceModel(formattedDistance, it)
            }
    }
}
