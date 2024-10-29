package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val description: String,
    val stationCode: String,
    val lat: Double,
    val lng: Double,
    val extra: LocationExtra,
    val link: Link,
    // Weirdly nullable, see Ermelo
    val openingHours: List<OpeningHoursDTO>? = null
)

@Serializable
data class Link(
    val uri: String
)

@Serializable
data class LocationExtra(
    val locationCode: String,
    val fetchTime: Long,
    val rentalBikes: Int? = null,
)