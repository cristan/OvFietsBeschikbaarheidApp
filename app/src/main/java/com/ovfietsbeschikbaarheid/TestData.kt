package com.ovfietsbeschikbaarheid

import com.ovfietsbeschikbaarheid.model.LocationOverviewModel

object TestData {
    val testLocationOverviewModel = LocationOverviewModel(
        "Hilversum Sportpark",
        "https://places.ns-mlab.nl/api/v2/places/stationfacility/Zelfservice%20OV-fiets%20uitgiftepunt-nvd001",
        "nvd001",
        "HVS",
        10,
        true,
        latitude = 52.36599,
        longitude = 6.469563,
    )
}