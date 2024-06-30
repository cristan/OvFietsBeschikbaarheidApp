package com.ovfietsbeschikbaarheid.mapper

import com.ovfietsbeschikbaarheid.dto.Location
import com.ovfietsbeschikbaarheid.dto.LocationsDTO
import com.ovfietsbeschikbaarheid.dto.OpenDTO
import com.ovfietsbeschikbaarheid.model.LocationEntryModel
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
            val locationsInSameStation = locations
                .filter { it.stationCode == toMap.stationCode && it != toMap }
                .map(::mapLocation)

            LocationOverviewModel(
                mapLocation(toMap),
                locationsInSameStation
            )
        }.sortedBy { it.entry.title }
    }

    private fun mapLocation(toMap: Location): LocationEntryModel {
        val description = if (toMap.description == "s-Hertogenbosch") "'s-Hertogenbosch" else toMap.description

        return LocationEntryModel(
            description,
            toMap.link.uri,
            toMap.extra.rentalBikes,
            toMap.open == OpenDTO.Yes
        )
    }
}