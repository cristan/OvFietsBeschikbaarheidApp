package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

/**
 * Every change in the number of rental bikes, each one relative to the change before it.
 * The first change is relative to 1970-01-01T00:00Z and 0 bikes.
 */
@Serializable
data class LocationHistoryDTO(
    val minuteDeltas: List<Long>,
    val rentalBikeDeltas: List<Int>,
)
