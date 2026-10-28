package nl.ovfietsbeschikbaarheid.mapper

import dev.jordond.compass.Coordinates
import kotlinx.datetime.LocalDateTime
import nl.ovfietsbeschikbaarheid.ext.distanceTo
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.NearestOpenLocationWithBikesModel
import nl.ovfietsbeschikbaarheid.model.OpenState

private const val MAX_DISTANCE_IN_METERS = 2000

class NearestOpenLocationWithBikesMapper(
    private val locationsMapper: LocationsMapper
) {
    fun getNearestOpenLocationWithBikes(
        openedLocation: LocationOverviewModel,
        allLocations: List<LocationOverviewModel>,
        dateTime: LocalDateTime
    ): NearestOpenLocationWithBikesModel? {
        val isEmptyClosedOrClosing = openedLocation.rentalBikesAvailable == 0 || isClosedOrClosing(getOpenState(openedLocation, dateTime))
        if (!isEmptyClosedOrClosing) {
            return null
        }

        val coordinates = Coordinates(openedLocation.latitude, openedLocation.longitude)
        val nearbyLocationsOfSameType = allLocations.filter {
            it.type == openedLocation.type && it.distanceTo(coordinates) <= MAX_DISTANCE_IN_METERS
        }
        return locationsMapper.withDistance(nearbyLocationsOfSameType, coordinates)
            .firstNotNullOfOrNull { (distance, location) ->
                val rentalBikesAvailable = location.rentalBikesAvailable
                val openState = getOpenState(location, dateTime)
                if (rentalBikesAvailable != null && rentalBikesAvailable > 0 && !isClosedOrClosing(openState)) {
                    NearestOpenLocationWithBikesModel(
                        title = location.title,
                        locationCode = location.locationCode,
                        fetchTime = location.fetchTime,
                        rentalBikesAvailable = rentalBikesAvailable,
                        distance = distance,
                        openState = openState
                    )
                } else {
                    null
                }
            }
    }

    private fun getOpenState(location: LocationOverviewModel, dateTime: LocalDateTime): OpenState? {
        return location.openingHours?.let { OpenStateMapper.getOpenState(location.locationCode, it, dateTime) }
    }

    private fun isClosedOrClosing(openState: OpenState?) = openState is OpenState.Closed || openState is OpenState.Closing
}
