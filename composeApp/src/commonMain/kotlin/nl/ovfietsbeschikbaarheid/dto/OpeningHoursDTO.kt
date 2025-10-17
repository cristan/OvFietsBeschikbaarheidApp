package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class OpeningHoursDTO(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val closesNextDay: Boolean
)