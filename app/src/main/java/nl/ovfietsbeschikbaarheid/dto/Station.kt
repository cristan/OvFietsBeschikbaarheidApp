package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class Station(
    val code: String,
    val name: String,
    val lat: Double,
    val lng: Double,
)