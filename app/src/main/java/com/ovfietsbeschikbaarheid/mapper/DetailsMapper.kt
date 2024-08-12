package com.ovfietsbeschikbaarheid.mapper

import com.google.android.gms.maps.model.LatLng
import com.ovfietsbeschikbaarheid.dto.DetailsDTO
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.OpeningHoursModel
import com.ovfietsbeschikbaarheid.repository.OverviewRepository

object DetailsMapper {
    val newLinesAtEnd = Regex("[\\\\n\\s]*\$")

    fun convert(
        detailsDTO: DetailsDTO,
        allLocations: List<LocationOverviewModel>,
        allStations: Map<String, String>
    ): DetailsModel {
        val payload = detailsDTO.payload

        val directions = payload.infoImages.find { it.title == "Routebeschrijving" }?.body?.replace(newLinesAtEnd, "")
        val about = payload.infoImages.find { it.title == "Bijzonderheden" }?.body?.replace(newLinesAtEnd, "")

        val location =
            if (payload.city == "" || payload.city == null || payload.street == null || payload.houseNumber == null || payload.postalCode == null) {
                null
            } else {
                LocationModel(
                    city = payload.city.trim(),
                    street = payload.street.trim(),
                    houseNumber = payload.houseNumber.trim(),
                    postalCode = payload.postalCode.trim().replace("  ", " "),
                )
            }

        val openingHoursModels = (payload.openingHours ?: emptyList()).map {
            OpeningHoursModel(
                dayOfWeek = getDayName(it.dayOfWeek),
                startTime = it.startTime,
                endTime = it.endTime
            )
        }

        val alternatives = allLocations.filter {
            // Find others with the same station code
            it.stationCode == payload.stationCode &&

                    // Except BSLC. This isn't a station, these are self service stations
                    it.stationCode != "BSLC" &&

                    // Don't pick yourself
                    it.locationCode != payload.extra.locationCode
        }

        val serviceType = payload.extra.serviceType
            ?: if (detailsDTO.self.uri.contains("Zelfservice")) "Zelfservice" else null
        return DetailsModel(
            description = payload.description,
            openingHours = openingHoursModels,
            rentalBikesAvailable = payload.extra.rentalBikes,
            serviceType = serviceType,
            directions = if (directions != "") directions else null,
            about = about,
            location = location,
            coordinates = LatLng(payload.lat, payload.lng),
            stationName = allStations[payload.stationCode],
            alternatives = alternatives
        )
    }

    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Maandag"
            2 -> "Dinsdag"
            3 -> "Woensdag"
            4 -> "Donderdag"
            5 -> "Vrijdag"
            6 -> "Zaterdag"
            7 -> "Zondag"
            else -> throw Exception("Unexpected day of week $dayOfWeek")
        }
    }
}