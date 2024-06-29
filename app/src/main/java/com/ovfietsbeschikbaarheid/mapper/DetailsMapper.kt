package com.ovfietsbeschikbaarheid.mapper

import com.google.android.gms.maps.model.LatLng
import com.ovfietsbeschikbaarheid.dto.DetailsDTO
import com.ovfietsbeschikbaarheid.dto.OpenDTO
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationModel
import com.ovfietsbeschikbaarheid.model.OpeningHoursModel

object DetailsMapper {
    fun convert(detailsDTO: DetailsDTO): DetailsModel {
        val payload = detailsDTO.payload

        val directions = payload.infoImages.find { it.title == "Routebeschrijving" }?.body
        val about = payload.infoImages.find { it.title == "Bijzonderheden" }?.body

        val location = if (payload.city == "") null else LocationModel(
            city = payload.city,
            street = payload.street,
            houseNumber = payload.houseNumber,
            postalCode = payload.postalCode,
        )

        val openingHoursModels = payload.openingHours.map {
            OpeningHoursModel(
                dayOfWeek = getDayName(it.dayOfWeek),
                startTime = it.startTime,
                endTime = it.endTime
            )
        }

        return DetailsModel(
            description = payload.description,
            open = payload.open == OpenDTO.Yes,
            openingHours = openingHoursModels,
            rentalBikesAvailable = payload.extra.rentalBikes,
            serviceType = payload.extra.serviceType,
            directions = if (directions != "") directions else null,
            about = about,
            location = location,
            coordinates = LatLng(payload.lat, payload.lng),
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