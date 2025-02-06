package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable

@Serializable
data class VehiclesDTO(
    /**
     * The timestamp when this was last updated. Example: 2025-02-04T16:37:02Z
     */
    val last_updated: String,

    /**
     * How long this data is valid for. Example: 60
     */
    val ttl: Int,

    val data: VehicleDataDTO
)

@Serializable
data class VehicleDataDTO(
    val vehicles: List<VehicleDTO>
)

@Serializable
data class VehicleDTO(
    /**
     * possible values: `donkey`, `moveyou`, `lime`, etc
     */
    val system_id: String,

    /**
     * Example: `lime:19fd6cb37336`
     */
    val vehicle_id: String,

    val lat: Double,
    val lon: Double,
    val is_reserved: Boolean,
    val is_disabled: Boolean,
    /**
     * possible values: `bicycle`, `cargo_bicycle`, `moped`, `car`. Can be null, but this is very rare
     */
    val form_factor: String?,

    /**
     * `electric_assist`, `null` or `human` for bikes and `electric` or `combustion` for cars
     */
    val propulsion_type: String?
)
