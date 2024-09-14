package nl.ovfietsbeschikbaarheid.mapper

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.dto.DetailsDTO
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.LocationModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OpeningHoursModel
import nl.ovfietsbeschikbaarheid.model.ServiceType
import timber.log.Timber
import java.time.LocalDateTime
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

object DetailsMapper {
    private val newLinesAtEnd = Regex("[\\\\n\\s]*\$")

    fun convert(
        detailsDTO: DetailsDTO,
        allLocations: List<LocationOverviewModel>,
        allStations: Map<String, String>,
        capacities: Map<String, Int>
    ): DetailsModel {
        val payload = detailsDTO.payload

        val directions = payload.infoImages.find { it.title == "Routebeschrijving" }?.body
            ?.replace(newLinesAtEnd, "")
            // Just for Rotterdam Kralingse Zoom
            ?.replace("&amp;", "&")
        val about = payload.infoImages.find { it.title == "Bijzonderheden" }?.body?.replace(newLinesAtEnd, "")
        // Filled in example Leiden Centraal, Centrumzijde
        val openingHoursInfo = payload.infoImages.find { it.title == "Info openingstijden" }?.body
        val disruptions = payload.infoImages.find { it.title == "Storing" }?.body

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

        val foundCapacity = capacities[payload.extra.locationCode.lowercase(Locale.UK)]
        if (foundCapacity == null) {
            Timber.w("No capacity found for ${payload.extra.locationCode}!")
        }
        val rentalBikesAvailable = payload.extra.rentalBikes
        val maxCapacity =
            if (foundCapacity != null && rentalBikesAvailable != null) {
                if (foundCapacity > rentalBikesAvailable) {
                    Timber.w("Found capacity $foundCapacity is greater than rental bikes available $rentalBikesAvailable!")
                    foundCapacity
                } else {
                    rentalBikesAvailable
                }
            } else foundCapacity ?: rentalBikesAvailable ?: 0


        val serviceType = when (payload.extra.serviceType) {
            "Bemenst" -> ServiceType.Bemenst
            "Kluizen" -> ServiceType.Kluizen
            "Sleutelautomaat" -> ServiceType.Sleutelautomaat
            "Box" -> ServiceType.Box
            null -> if (detailsDTO.self.uri.contains("Zelfservice", ignoreCase = true)) ServiceType.Zelfservice else null
            else -> {
                Timber.w("Unknown service type: ${payload.extra.serviceType}")
                null
            }
        }

        return DetailsModel(
            description = payload.description,
            openingHoursInfo = openingHoursInfo,
            openingHours = openingHoursModels,
            rentalBikesAvailable = rentalBikesAvailable,
            capacity = max(rentalBikesAvailable ?: 0, maxCapacity),
            serviceType = serviceType,
            directions = if (directions != "") directions else null,
            about = about,
            disruptions = disruptions,
            location = location,
            coordinates = LatLng(payload.lat, payload.lng),
            stationName = allStations[payload.stationCode],
            alternatives = alternatives,
            openState = payload.openingHours?.let {
                OpenStateMapper.getOpenState(
                    it, LocalDateTime.now(TimeZone.getTimeZone("Europe/Amsterdam").toZoneId())
                )
            },
        )
    }

    @StringRes
    fun getDayName(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            1 -> R.string.day_1
            2 -> R.string.day_2
            3 -> R.string.day_3
            4 -> R.string.day_4
            5 -> R.string.day_5
            6 -> R.string.day_6
            7 -> R.string.day_7
            else -> throw Exception("Unexpected day of week $dayOfWeek")
        }
    }
}