package com.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class DetailsDTO(
    val payload: DetailsPayload
)



@Serializable
data class DetailsPayload(
    val description: String,

    val lat: Double,
    val lng: Double,

    // `Yes` or `No`
    val open: OpenDTO,

    // These 4 aren't nullable, but can be an empty string
    val city: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,

    val extra: PayloadExtra,

    val openingHours: List<OpeningHours>,
    val infoImages: List<InfoImage>,
)

@Serializable
data class PayloadExtra(
    // a string containing an Int. Weirdly nullable (see Raalte)
    val rentalBikes: Int? = null,

    // Can be Bemenst, Kluizen, Sleutelautomaat
    val serviceType: String? = null
)

@Serializable
data class OpeningHours(
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val closesNextDay: Boolean,
)

@Serializable
data class InfoImage(
    val title: String,
    val body: String,
)

