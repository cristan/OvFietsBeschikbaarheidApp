package nl.ovfietsbeschikbaarheid.model

data class LocationOverviewModel(
    val title: String,
    val uri: String,
    val locationCode: String,
    val stationCode: String,
    val latitude: Double,
    val longitude: Double,
)

data class LocationOverviewWithDistanceModel(
    val distance: String,
    val location: LocationOverviewModel
)