package nl.ovfietsbeschikbaarheid

import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationType

object TestData {
    val testLocationOverviewModel = LocationOverviewModel(
        "Amersfoort Centraal",
        "https://places.ns-mlab.nl/api/v2/places/stationfacility/Zelfservice%20OV-fiets%20uitgiftepunt-nvd001",
        "nvd001",
        "HVS",
        latitude = 52.36599,
        longitude = 6.469563,
        type = LocationType.Regular
    )
}