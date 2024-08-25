package com.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationsDTO(
    val locaties: Map<String, Location>
)

@Serializable
data class Location(
    val description: String,
    val city: String? = null,
    val street: String? = null,
    val houseNumber: String? = null,
    val postalCode: String? = null,
    val stationCode: String,
    val open: OpenDTO,
    val lat: Double,
    val lng: Double,
    val extra: LocationExtra,
    val link: Link
)

@Serializable
data class Link(
    val uri: String
)

@Serializable
data class LocationExtra(
// Can be Bemenst, Kluizen, Sleutelautomaat
    val serviceType: String? = null,
    // basically an Int. Weirdly nullable (see Raalte)
    val rentalBikes: Int? = null,
//    "type": "OV_FIETS",
    val fetchTime: Long,
    val locationCode: String,
)