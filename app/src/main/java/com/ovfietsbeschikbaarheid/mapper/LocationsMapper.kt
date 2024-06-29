package com.ovfietsbeschikbaarheid.mapper

import com.ovfietsbeschikbaarheid.dto.LocationsDTO
import com.ovfietsbeschikbaarheid.dto.OpenDTO
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel

object LocationsMapper {
    private val nonExistingLocations = listOf(
        "asb003",
        "UTVR002",
        "ut018",
        "UTVR002",
    )

    fun map(locationsDTO: LocationsDTO): List<LocationOverviewModel> {
        val locations = locationsDTO.locaties.values
            .filter { !nonExistingLocations.contains(it.extra.locationCode) }
            .sortedBy { it.description }

        return locations.map {
            LocationOverviewModel(
                it.description,
                it.link.uri,
                it.extra.rentalBikes,
                it.open == OpenDTO.Yes
            )
        }
    }
}