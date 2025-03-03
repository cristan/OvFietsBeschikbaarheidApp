package nl.ovfietsbeschikbaarheid

import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationType

object TestData {
    val testLocationOverviewModel = LocationOverviewModel(
        "Amersfoort Centraal",
        288,
        "https://places.ns-mlab.nl/api/v2/places/stationfacility/Zelfservice%20OV-fiets%20uitgiftepunt-nvd001",
        1729602804,
        "nvd001",
        "HVS",
        locationPosition = LatLng(52.36599, 6.469563),
        type = LocationType.Regular,
        openingHours = emptyList()
    )
}