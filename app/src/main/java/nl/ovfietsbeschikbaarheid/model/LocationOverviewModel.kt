package nl.ovfietsbeschikbaarheid.model

import nl.ovfietsbeschikbaarheid.dto.OpeningHoursDTO

data class LocationOverviewModel(
    val title: String,
    val rentalBikesAvailable: Int?,
    val uri: String,
    val fetchTime: Long,
    val locationCode: String,
    val stationCode: String,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType,
    val openingHours: List<OpeningHoursDTO>?
)

data class LocationOverviewWithDistanceModel(
    val distance: String,
    val location: LocationOverviewModel
)

enum class LocationType {
    Regular, EBike
}