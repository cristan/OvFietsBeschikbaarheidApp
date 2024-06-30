package com.ovfietsbeschikbaarheid.model

data class LocationOverviewModel(
    val title: String,
    val uri: String,
    val locationCode: String,
    val stationCode: String,
    val rentalBikesAvailable: Int?,
    val open: Boolean,
)