package nl.ovfietsbeschikbaarheid.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import nl.ovfietsbeschikbaarheid.dto.OpeningHoursDTO

data class LocationOverviewModel(
    val locationTitle: String,
    val rentalBikesAvailable: Int?,
    val uri: String,
    val fetchTime: Long,
    val locationCode: String,
    val stationCode: String,
    val locationPosition: LatLng,
    val type: LocationType,
    val openingHours: List<OpeningHoursDTO>?
) : ClusterItem {
    override fun getPosition() = locationPosition

    override fun getTitle() = locationTitle

    override fun getSnippet() = ""

    override fun getZIndex() = 0f
}

data class LocationOverviewWithDistanceModel(
    val distance: String,
    val location: LocationOverviewModel
)

enum class LocationType {
    Regular, EBike
}