package nl.ovfietsbeschikbaarheid.model

import kotlinx.serialization.Serializable

@Serializable
data class Favourite(
    val locationCode: String,
    val title: String,
)
