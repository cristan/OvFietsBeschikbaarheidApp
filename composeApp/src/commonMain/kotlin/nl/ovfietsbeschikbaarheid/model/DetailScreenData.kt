package nl.ovfietsbeschikbaarheid.model

import kotlinx.serialization.Serializable

@Serializable
data class DetailScreenData(val title: String, val locationCode: String, val fetchTime: Long)