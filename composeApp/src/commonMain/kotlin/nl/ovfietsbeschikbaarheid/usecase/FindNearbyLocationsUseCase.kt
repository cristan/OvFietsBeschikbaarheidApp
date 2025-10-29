package nl.ovfietsbeschikbaarheid.usecase

import co.touchlab.kermit.Logger
import dev.jordond.compass.geocoder.Geocoder
import nl.ovfietsbeschikbaarheid.ext.isInTheNetherlands
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel

class FindNearbyLocationsUseCase(
    private val geocoder: Geocoder,
    private val locationsMapper: LocationsMapper,
) {
    suspend operator fun invoke(
        searchTerm: String,
        allLocations: List<LocationOverviewModel>
    ): List<LocationOverviewWithDistanceModel>? {
        val geoCoderAvailable = geocoder.isAvailable()
        if (!geoCoderAvailable) {
            Logger.w("No geocoder available!")
            return null
        }

        val coordinates = geocoder.forward(searchTerm).getOrNull()

        return if (coordinates != null) {
            val foundCoordinates = coordinates.find { it.isInTheNetherlands() } ?: coordinates[0]
            locationsMapper.withDistance(allLocations, foundCoordinates)
        } else {
            null
        }
    }
}