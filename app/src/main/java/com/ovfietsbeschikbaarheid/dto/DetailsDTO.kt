package com.ovfietsbeschikbaarheid.dto

import com.ovfietsbeschikbaarheid.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class DetailsDTO(
    val payload: DetailsPayload
)

enum class Open {
    Yes, No
}

@Serializable
data class DetailsPayload(
    val description: String,

    val lat: Double,
    val lng: Double,

    // `Yes` or `No`
    val open: Open,

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

