package com.ovfietsbeschikbaarheid.mapper

import com.google.android.gms.maps.model.LatLng
import com.ovfietsbeschikbaarheid.dto.DetailsDTO
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.OpeningHoursModel

object DetailsMapper {
    fun convert(
        detailsDTO: DetailsDTO,
        locationOverviewModel: LocationOverviewModel
    ): DetailsModel {
        val payload = detailsDTO.payload

        val directions = payload.infoImages.find { it.title == "Routebeschrijving" }?.body
        val about = payload.infoImages.find { it.title == "Bijzonderheden" }?.body

        val location =
            if (payload.city == "" || payload.city == null || payload.street == null || payload.houseNumber == null || payload.postalCode == null) {
                null
            } else {
                LocationModel(
                    city = payload.city,
                    street = payload.street,
                    houseNumber = payload.houseNumber,
                    postalCode = payload.postalCode,
                )
            }

        val openingHoursModels = (payload.openingHours ?: emptyList()).map {
            OpeningHoursModel(
                dayOfWeek = getDayName(it.dayOfWeek),
                startTime = it.startTime,
                endTime = it.endTime
            )
        }

        return DetailsModel(
            description = payload.description,
            openingHours = openingHoursModels,
            rentalBikesAvailable = payload.extra.rentalBikes,
            serviceType = payload.extra.serviceType,
            directions = if (directions != "") directions else null,
            about = about,
            location = location,
            coordinates = LatLng(payload.lat, payload.lng),
            alternatives = locationOverviewModel.alternatives
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