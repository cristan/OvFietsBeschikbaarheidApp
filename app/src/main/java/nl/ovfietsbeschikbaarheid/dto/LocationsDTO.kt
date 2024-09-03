package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationsDTO(
    val locaties: Map<String, Location>
)

@Serializable
data class Location(
    val description: String,
    val stationCode: String,
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
    val locationCode: String,
)