package com.ovfietsbeschikbaarheid.mapper

import com.ovfietsbeschikbaarheid.dto.Location
import com.ovfietsbeschikbaarheid.dto.LocationsDTO
import com.ovfietsbeschikbaarheid.dto.OpenDTO
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel

object LocationsMapper {
    private val nonExistingLocations = listOf(
        "asb003",
        "UTVR002",
        "ut018",
        "UTVR002",
        "ehv004",
        "gvc021",
    )

    fun map(locationsDTO: LocationsDTO): List<LocationOverviewModel> {
        val locations = locationsDTO.locaties.values
            .filter { !nonExistingLocations.contains(it.extra.locationCode) }

        return locations.map { toMap ->
            val description = if (toMap.description == "s-Hertogenbosch") "'s-Hertogenbosch" else toMap.description
            LocationOverviewModel(
                title = description,
                uri = toMap.link.uri,
                locationCode = toMap.extra.locationCode,
                stationCode = toMap.stationCode,
                rentalBikesAvailable = toMap.extra.rentalBikes,
                open = (toMap.open == OpenDTO.Yes || toMap.open == OpenDTO.Unknown)
            )
        }.sortedBy { it.title }
    }
}