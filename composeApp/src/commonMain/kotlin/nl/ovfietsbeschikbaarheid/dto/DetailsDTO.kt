package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

/**
 * The DTO from the m-labs call. Has almost as much info as the LocationsDTO (except for the address), but we only use the amount
 */
@Serializable
data class DetailsDTO(
    val payload: DetailsPayload,
)

@Serializable
data class DetailsPayload(
    val extra: PayloadExtra,
)

@Serializable
data class PayloadExtra(
    // a string containing an Int. Weirdly nullable (see Delft Zuid)
    val rentalBikes: Int? = null,
)