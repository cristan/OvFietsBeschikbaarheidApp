package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class StationFacilitiesDTO(
    val payload: List<StationFacilityDTO>,
)

@Serializable
data class StationFacilityDTO(
    val name: String,
)
