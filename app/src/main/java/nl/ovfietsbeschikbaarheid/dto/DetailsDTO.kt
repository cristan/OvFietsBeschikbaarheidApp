package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class DetailsDTO(
    val payload: DetailsPayload,
    val self: Self
)

@Serializable
data class Self(
    val uri: String
)

@Serializable
data class DetailsPayload(
    val description: String,
    val stationCode: String,

    val lat: Double,
    val lng: Double,

    // Enkhuizen says it's Unknown, but it's a box which is therefore open 24/7. As far as I know, that applies to the other 3 unknowns.
    val open: OpenDTO,

    // These 4 are nullable (Enkhuizen), but can also be an empty string (Veenendaal-De klomp)
    val city: String? = null,
    val street: String? = null,
    val houseNumber: String? = null,
    val postalCode: String? = null,

    val extra: PayloadExtra,

    val openingHours: List<OpeningHoursDTO>? = null,
    val infoImages: List<InfoImage>,
)

@Serializable
data class PayloadExtra(
    // a string containing an Int. Weirdly nullable (see Delft Zuid)
    val rentalBikes: Int? = null,

    // Can be Bemenst, Kluizen, Sleutelautomaat, Box (Enkhuizen) or null (Utrecht Terwijde)
    val serviceType: String? = null,

    val locationCode: String,
)

@Serializable
data class InfoImage(
    val title: String,
    val body: String,
)

