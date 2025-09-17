package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val description: String,
    val stationCode: String,

    val lat: Double,
    val lng: Double,

    // These 4 can be empty string (Veenendaal-De klomp), but the NS backend doesn't return this anymore at all as of late
    val city: String? = null,
    val street: String? = null,
    val houseNumber: String? = null,
    val postalCode: String? = null,

    val extra: LocationExtra,
    val link: Link,

    // Weirdly nullable, see Ermelo
    val openingHours: List<OpeningHoursDTO>? = null,
    val infoImages: List<InfoImage>,
)

@Serializable
data class Link(
    val uri: String
)

@Serializable
data class LocationExtra(
    // a string containing an Int. Weirdly nullable (see Delft Zuid)
    val rentalBikes: Int? = null,
    val locationCode: String,
    val fetchTime: Long,
    // Can be Bemenst, Kluizen, Sleutelautomaat, Box (Enkhuizen) or null (Utrecht Terwijde)
    val serviceType: String? = null,
)

@Serializable
data class InfoImage(
    val title: String,
    val body: String,
)