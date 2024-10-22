package nl.ovfietsbeschikbaarheid.model

data class LocationOverviewModel(
    val title: String,
    val uri: String,
    val fetchTime: Long,
    val locationCode: String,
    val stationCode: String,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType
)

data class LocationOverviewWithDistanceModel(
    val distance: String,
    val location: LocationOverviewModel
)

enum class LocationType {
    Regular, EBike
}