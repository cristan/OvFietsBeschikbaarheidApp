package com.ovfietsbeschikbaarheid.model

import com.google.android.gms.maps.model.LatLng

data class DetailsModel(
    val description: String,
    val openingHours: List<OpeningHoursModel>,
    val rentalBikesAvailable: Int?,
    val capacity: Int,
    val serviceType: String?,
    val about: String?,
    val directions: String?,
    val location: LocationModel?,
    val coordinates: LatLng,
    val stationName: String?,
    val alternatives: List<LocationOverviewModel>,
)

data class LocationModel(
    val city: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
)

data class OpeningHoursModel(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
)