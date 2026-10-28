package nl.ovfietsbeschikbaarheid.model

data class MapMarker(
    val latitude: Double,
    val longitude: Double,
    val locations: List<LocationOverviewModel>,
)
