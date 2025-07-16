package nl.ovfietsbeschikbaarheid.mapper

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.dto.DetailsDTO
import nl.ovfietsbeschikbaarheid.dto.HourlyLocationCapacityDto
import nl.ovfietsbeschikbaarheid.ext.atEndOfDay
import nl.ovfietsbeschikbaarheid.ext.atStartOfDay
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.GraphDayModel
import nl.ovfietsbeschikbaarheid.model.LocationModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OpeningHoursModel
import nl.ovfietsbeschikbaarheid.model.ServiceType
import nl.ovfietsbeschikbaarheid.util.dutchLocale
import nl.ovfietsbeschikbaarheid.util.dutchZone
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

object DetailsMapper {
    private val newLinesAtEnd = Regex("[\\\\n\\s]*\$")

    fun convert(
        detailsDTO: DetailsDTO,
        allLocations: List<LocationOverviewModel>,
        allStations: Map<String, String>,
        capacities: Map<String, Int>,
        hourlyLocationCapacityDtos: List<HourlyLocationCapacityDto>
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
        }.map { DetailScreenData(it.title, it.uri, it.locationCode, it.fetchTime) }

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
        val graphDays = getGraphDays(rentalBikesAvailable, hourlyLocationCapacityDtos)


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
                    payload.extra.locationCode, it, LocalDateTime.now(TimeZone.getTimeZone("Europe/Amsterdam").toZoneId())
                )
            },
            graphDays = graphDays
        )
    }

    private fun getGraphDays(
        rentalBikesAvailable: Int?,
        hourlyLocationCapacityDtos: List<HourlyLocationCapacityDto>
    ): List<GraphDayModel> {
        val nowInNL = ZonedDateTime.now(dutchZone)

        // Let's add the current capacity to the graph. Unfortunately, the current backend call doesn't return a timestamp, so we'll assume now.
        val currentCapacity = CapacityModel(rentalBikesAvailable ?: 0, nowInNL)
        val historicalCapacities = convertHourlyCapacities(hourlyLocationCapacityDtos).sortedBy { it.dateTime } + currentCapacity

        val dayOfWeek = nowInNL.get(ChronoField.DAY_OF_WEEK)

        val previousDays = (dayOfWeek -1  downTo 1).map { previousDayOffset ->
            val previousDay = nowInNL.minusDays(previousDayOffset.toLong())
            val startOfDay = previousDay.atStartOfDay()
            // We also want the 00:00 hours to complete the day, but that one usually only arrives at something like 00:01
            val endOfDay = previousDay.atEndOfDay().plusMinutes(10)
            val capacitiesPastDay = historicalCapacities.filter { it.dateTime.isAfter(startOfDay) && it.dateTime.isBefore(endOfDay) }
            GraphDayModel(
                isToday = false,
                previousDay.dayOfWeek.getDisplayName(TextStyle.NARROW, dutchLocale),
                previousDay.dayOfWeek.getDisplayName(TextStyle.FULL, dutchLocale),
                capacitiesPastDay,
                emptyList()
            )
        }

        val graphToday = getGraphToday(nowInNL, historicalCapacities)

        val nextDays = (1 .. 7 - dayOfWeek).map { nextDayOffset ->
            val previousDay = nowInNL.plusDays(nextDayOffset.toLong() - 7L)
            val startOfDay = previousDay.atStartOfDay()
            // We also want the 00:00 hours to complete the day, but that one usually only arrives at something like 00:01
            val endOfDay = previousDay.atEndOfDay().plusMinutes(10)

            val capacitiesFutureDay = historicalCapacities.filter { it.dateTime.isAfter(startOfDay) && it.dateTime.isBefore(endOfDay) }
            GraphDayModel(
                isToday = false,
                previousDay.dayOfWeek.getDisplayName(TextStyle.NARROW, dutchLocale),
                previousDay.dayOfWeek.getDisplayName(TextStyle.FULL, dutchLocale),
                emptyList(),
                capacitiesFutureDay,
            )
        }

        val graphDays = previousDays + listOf(graphToday) + nextDays
        return graphDays
    }

    private fun getGraphToday(
        nowInNL: ZonedDateTime,
        historicalCapacities: List<CapacityModel>
    ): GraphDayModel {
        val startOfDay = nowInNL.atStartOfDay()
        val capacitiesToday = historicalCapacities.filter { it.dateTime.isAfter(startOfDay) }
        val startLastWeek = nowInNL.minusDays(7).plusHours(1).truncatedTo(ChronoUnit.HOURS)

        // plusHours(1) will you straight into next day when it's past 23:00
        val endLastWeek = if (startLastWeek.hour == 0) {
            startLastWeek.plusMinutes(10)
        } else {
            // We also want the 00:00 hours to complete the day, but that one usually only arrives at something like 00:01
            startLastWeek.atEndOfDay().plusMinutes(10)
        }
        val capacitiesPrediction = historicalCapacities.filter { it.dateTime >= startLastWeek && it.dateTime <= endLastWeek }

        val graphToday = GraphDayModel(
            isToday = true,
            nowInNL.dayOfWeek.getDisplayName(TextStyle.NARROW, dutchLocale),
            nowInNL.dayOfWeek.getDisplayName(TextStyle.FULL, dutchLocale),
            capacitiesToday,
            capacitiesPrediction
        )
        return graphToday
    }

    private fun convertHourlyCapacities(hourlyLocationCapacityDtos: List<HourlyLocationCapacityDto>): List<CapacityModel> {
        return hourlyLocationCapacityDtos.map {
            val firstCapacity = it.document.fields.first.integerValue.toInt()
            val dateTime = it.document.createTime
            CapacityModel(
                firstCapacity,
                dateTime.withZoneSameInstant(dutchZone)
            )
        }
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