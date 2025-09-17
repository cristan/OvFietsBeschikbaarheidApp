package nl.ovfietsbeschikbaarheid.mapper

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.dto.HourlyLocationCapacityDto
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.ext.atEndOfDay
import nl.ovfietsbeschikbaarheid.ext.atStartOfDay
import nl.ovfietsbeschikbaarheid.model.AddressModel
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.GraphDayModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OpeningHoursModel
import nl.ovfietsbeschikbaarheid.model.ServiceType
import nl.ovfietsbeschikbaarheid.util.Translator
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

class DetailsMapper(
    private val translator: Translator
) {
    private val newLinesAtEnd = Regex("[\\\\n\\s]*\$")

    fun convert(
        locationDTO: LocationDTO,
        allLocations: List<LocationOverviewModel>,
        allStations: Map<String, String>,
        capacities: Map<String, Int>,
        hourlyLocationCapacityDtos: List<HourlyLocationCapacityDto>
    ): DetailsModel {

        val directions = locationDTO.infoImages.find { it.title == "Routebeschrijving" }?.body
            ?.replace(newLinesAtEnd, "")
            // Just for Rotterdam Kralingse Zoom
            ?.replace("&amp;", "&")
        val about = locationDTO.infoImages.find { it.title == "Bijzonderheden" }?.body?.replace(newLinesAtEnd, "")
        // Filled in example Leiden Centraal, Centrumzijde
        val openingHoursInfo = locationDTO.infoImages.find { it.title == "Info openingstijden" }?.body
        val disruptions = locationDTO.infoImages.find { it.title == "Storing" }?.body

        val location =
            if (locationDTO.city == "" || locationDTO.city == null || locationDTO.street == null || locationDTO.houseNumber == null || locationDTO.postalCode == null) {
                null
            } else {
                AddressModel(
                    city = locationDTO.city.trim(),
                    street = locationDTO.street.trim(),
                    houseNumber = locationDTO.houseNumber.trim(),
                    postalCode = locationDTO.postalCode.trim().replace("  ", " "),
                )
            }

        val openingHoursModels = (locationDTO.openingHours ?: emptyList()).map {
            OpeningHoursModel(
                dayOfWeek = getDayName(it.dayOfWeek),
                startTime = it.startTime,
                endTime = it.endTime
            )
        }

        val alternatives = allLocations.filter {
            // Find others with the same station code
            it.stationCode == locationDTO.stationCode &&

                    // Except BSLC. This isn't a station, these are self service stations
                    it.stationCode != "BSLC" &&

                    // Don't pick yourself
                    it.locationCode != locationDTO.extra.locationCode
        }.map { DetailScreenData(it.title, it.uri, it.locationCode, it.fetchTime) }

        val foundCapacity = capacities[locationDTO.extra.locationCode.lowercase(Locale.UK)]
        if (foundCapacity == null) {
            Timber.w("No capacity found for ${locationDTO.extra.locationCode}!")
        }
        val rentalBikesAvailable = locationDTO.extra.rentalBikes
        val maxCapacity =
            if (foundCapacity != null && rentalBikesAvailable != null) {
                if (rentalBikesAvailable > foundCapacity) {
                    Timber.w("Rental bikes available $rentalBikesAvailable is greater than the capacity $foundCapacity!")
                    rentalBikesAvailable
                } else {
                    foundCapacity
                }
            } else foundCapacity ?: rentalBikesAvailable ?: 0
        val maxCapacityFromHistory = hourlyLocationCapacityDtos.maxOfOrNull { it.document.fields.first.integerValue.toInt() } ?: 0

        val serviceType = when (locationDTO.extra.serviceType) {
            "Bemenst" -> ServiceType.Bemenst
            "Kluizen" -> ServiceType.Kluizen
            "Sleutelautomaat" -> ServiceType.Sleutelautomaat
            "Box" -> ServiceType.Box
            null -> if (locationDTO.link.uri.contains("Zelfservice", ignoreCase = true)) ServiceType.Zelfservice else null
            else -> {
                Timber.w("Unknown service type: ${locationDTO.extra.serviceType}")
                null
            }
        }
        val graphDays = getGraphDays(rentalBikesAvailable, hourlyLocationCapacityDtos)


        return DetailsModel(
            description = locationDTO.description,
            openingHoursInfo = openingHoursInfo,
            openingHours = openingHoursModels,
            rentalBikesAvailable = rentalBikesAvailable,
            capacity = max(maxCapacity, maxCapacityFromHistory),
            serviceType = serviceType,
            directions = if (directions != "") directions else null,
            about = about,
            disruptions = disruptions,
            location = location,
            coordinates = LatLng(locationDTO.lat, locationDTO.lng),
            stationName = allStations[locationDTO.stationCode],
            alternatives = alternatives,
            openState = locationDTO.openingHours?.let {
                OpenStateMapper.getOpenState(
                    locationDTO.extra.locationCode, it, LocalDateTime.now(TimeZone.getTimeZone("Europe/Amsterdam").toZoneId())
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

            // A fallback to 0 doesn't make too much sense, but this prevents a crash and empty days will be filtered out later
            val minCapacity = capacitiesPastDay.minOfOrNull { it.capacity } ?: 0
            val maxCapacity = capacitiesPastDay.maxOfOrNull { it.capacity } ?: 0
            val dayFullName = previousDay.dayOfWeek.getDisplayName(TextStyle.FULL, dutchLocale)
            val contentDescription = translator.getString(R.string.graph_previous_day_content_description, dayFullName, minCapacity, maxCapacity)
            GraphDayModel(
                isToday = false,
                previousDay.dayOfWeek.getDisplayName(TextStyle.NARROW, dutchLocale),
                dayFullName,
                capacitiesPastDay,
                emptyList(),
                contentDescription
            )
        }

        val graphToday = getGraphToday(nowInNL, historicalCapacities)

        val nextDays = (1 .. 7 - dayOfWeek).map { nextDayOffset ->
            val previousDay = nowInNL.plusDays(nextDayOffset.toLong() - 7L)
            val startOfDay = previousDay.atStartOfDay()
            // We also want the 00:00 hours to complete the day, but that one usually only arrives at something like 00:01
            val endOfDay = previousDay.atEndOfDay().plusMinutes(10)

            val capacitiesFutureDay = historicalCapacities.filter { it.dateTime.isAfter(startOfDay) && it.dateTime.isBefore(endOfDay) }
            // A fallback to 0 doesn't make too much sense, but this prevents a crash and empty days will be filtered out later
            val minCapacity = capacitiesFutureDay.minOfOrNull { it.capacity } ?: 0
            val maxCapacity = capacitiesFutureDay.maxOfOrNull { it.capacity } ?: 0
            val dayFullName = previousDay.dayOfWeek.getDisplayName(TextStyle.FULL, dutchLocale)
            val contentDescription = translator.getString(R.string.graph_next_day_content_description, dayFullName, minCapacity, maxCapacity)
            GraphDayModel(
                isToday = false,
                previousDay.dayOfWeek.getDisplayName(TextStyle.NARROW, dutchLocale),
                dayFullName,
                emptyList(),
                capacitiesFutureDay,
                contentDescription
            )
        }

        val graphDays = previousDays + listOf(graphToday) + nextDays
        if (graphDays.any { it.capacityHistory.isEmpty() && it.capacityPrediction.isEmpty() }) {
            Timber.e("Empty graph days found! Returning nothing to prevent weird issues.")
            return emptyList()
        }
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

        // A fallback to 0 doesn't make too much sense, but this prevents a crash and empty days will be filtered out later
        val minCapacityToday = capacitiesToday.minOfOrNull { it.capacity } ?: 0
        val maxCapacityToday = capacitiesToday.maxOfOrNull { it.capacity } ?: 0
        val minCapacityPrediction = capacitiesPrediction.minOfOrNull { it.capacity } ?: 0
        val maxCapacityPrediction = capacitiesPrediction.maxOfOrNull { it.capacity } ?: 0
        val contentDescription = translator.getString(R.string.graph_today_content_description, minCapacityToday, maxCapacityToday, minCapacityPrediction, maxCapacityPrediction)

        val graphToday = GraphDayModel(
            isToday = true,
            nowInNL.dayOfWeek.getDisplayName(TextStyle.NARROW, dutchLocale),
            nowInNL.dayOfWeek.getDisplayName(TextStyle.FULL, dutchLocale),
            capacitiesToday,
            capacitiesPrediction,
            contentDescription
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

    companion object {
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
}