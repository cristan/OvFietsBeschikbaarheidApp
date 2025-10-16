package nl.ovfietsbeschikbaarheid.mapper

import com.google.android.gms.maps.model.LatLng
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import nl.ovfietsbeschikbaarheid.dto.HourlyLocationCapacityDto
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.ext.atEndOfDayIn
import nl.ovfietsbeschikbaarheid.ext.dutchTimeZone
import nl.ovfietsbeschikbaarheid.ext.getServiceType
import nl.ovfietsbeschikbaarheid.ext.toFullDayOfWeek
import nl.ovfietsbeschikbaarheid.ext.toIsoDayNumber
import nl.ovfietsbeschikbaarheid.ext.toNarrowDayOfWeek
import nl.ovfietsbeschikbaarheid.ext.truncateToHour
import nl.ovfietsbeschikbaarheid.model.AddressModel
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.GraphDayModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OpeningHoursModel
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.day_1
import nl.ovfietsbeschikbaarheid.resources.day_2
import nl.ovfietsbeschikbaarheid.resources.day_3
import nl.ovfietsbeschikbaarheid.resources.day_4
import nl.ovfietsbeschikbaarheid.resources.day_5
import nl.ovfietsbeschikbaarheid.resources.day_6
import nl.ovfietsbeschikbaarheid.resources.day_7
import nl.ovfietsbeschikbaarheid.resources.graph_next_day_content_description
import nl.ovfietsbeschikbaarheid.resources.graph_previous_day_content_description
import nl.ovfietsbeschikbaarheid.resources.graph_today_content_description
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import timber.log.Timber
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DetailsMapper() {
    private val newLinesAtEnd = Regex("[\\\\n\\s]*\$")

    suspend fun convert(
        locationDTO: LocationDTO,
        allLocations: List<LocationOverviewModel>,
        allStations: Map<String, String>,
        hourlyLocationCapacityDtos: List<HourlyLocationCapacityDto>
    ): DetailsModel {

        val directions = locationDTO.infoImages.find { it.title == "Routebeschrijving" }?.body
            ?.replace(newLinesAtEnd, "")
        val about = locationDTO.infoImages.find { it.title == "Bijzonderheden" }?.body?.replace(newLinesAtEnd, "")
        // Filled in for example at Leiden Centraal, Centrumzijde
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
        }.map { DetailScreenData(it.title, it.locationCode, it.fetchTime) }

        val rentalBikesAvailable = locationDTO.extra.rentalBikes

        val graphDays = getGraphDays(rentalBikesAvailable, hourlyLocationCapacityDtos)


        return DetailsModel(
            description = locationDTO.description,
            openingHoursInfo = openingHoursInfo,
            openingHours = openingHoursModels,
            rentalBikesAvailable = rentalBikesAvailable,
            capacity = locationDTO.maxCapacity,
            serviceType = locationDTO.getServiceType(),
            directions = if (directions != "") directions else null,
            about = about,
            disruptions = disruptions,
            location = location,
            coordinates = LatLng(locationDTO.lat, locationDTO.lng),
            stationName = allStations[locationDTO.stationCode],
            alternatives = alternatives,
            openState = locationDTO.openingHours?.let {
                OpenStateMapper.getOpenState(
                    locationDTO.extra.locationCode, it, Clock.System.now().toLocalDateTime(dutchTimeZone)
                )
            },
            graphDays = graphDays
        )
    }

    private suspend fun getGraphDays(
        rentalBikesAvailable: Int?,
        hourlyLocationCapacityDtos: List<HourlyLocationCapacityDto>
    ): List<GraphDayModel> {
        val now = Clock.System.now()
        val nowInNL: LocalDateTime = now.toLocalDateTime(dutchTimeZone)

        // Let's add the current capacity to the graph. Unfortunately, the current backend call doesn't return a timestamp, so we'll assume now.
        val currentCapacity = CapacityModel(rentalBikesAvailable ?: 0, now)
        val historicalCapacities = convertHourlyCapacities(hourlyLocationCapacityDtos).sortedBy { it.createTime } + currentCapacity

        val currentDayOfWeek = nowInNL.date.dayOfWeek.toIsoDayNumber()


        val previousDays = (currentDayOfWeek - 1 downTo 1).map { previousDayOffset ->
            val previousDay = now.minus(previousDayOffset, DateTimeUnit.DAY, dutchTimeZone)

            val previousDate = previousDay.toLocalDateTime(dutchTimeZone).date
            val startOfDay = previousDate.atStartOfDayIn(dutchTimeZone)
            // We also want the 00:00 hours to complete the day, but that one usually only arrives at something like 00:01
            val endOfDay = previousDay.toLocalDateTime(dutchTimeZone).atEndOfDayIn(dutchTimeZone).plus(10, DateTimeUnit.MINUTE)
            val capacitiesPastDay = historicalCapacities.filter { it.createTime > startOfDay && it.createTime < endOfDay }

            // A fallback to 0 doesn't make too much sense, but this prevents a crash and empty days will be filtered out later
            val minCapacity = capacitiesPastDay.minOfOrNull { it.capacity } ?: 0
            val maxCapacity = capacitiesPastDay.maxOfOrNull { it.capacity } ?: 0
            val dayFullName = previousDate.toFullDayOfWeek()
            val contentDescription = getString(Res.string.graph_previous_day_content_description, dayFullName, minCapacity, maxCapacity)
            GraphDayModel(
                isToday = false,
                previousDate.toNarrowDayOfWeek(),
                dayFullName,
                capacitiesPastDay,
                emptyList(),
                contentDescription
            )
        }

        val graphToday = getGraphToday(now, nowInNL, historicalCapacities)

        val nextDays = (1..7 - currentDayOfWeek).map { nextDayOffset ->
            val previousDay = now.plus(nextDayOffset - 7, DateTimeUnit.DAY, dutchTimeZone)
            val previousDayDateTime = previousDay.toLocalDateTime(dutchTimeZone)
            val previousDate = previousDayDateTime.date
            val startOfDay = previousDate.atStartOfDayIn(dutchTimeZone)
            // We also want the 00:00 hours to complete the day, but that one usually only arrives at something like 00:01
            val endOfDay = previousDayDateTime.atEndOfDayIn(dutchTimeZone).plus(10, DateTimeUnit.MINUTE)

            val capacitiesFutureDay = historicalCapacities.filter { it.createTime > startOfDay && it.createTime < endOfDay }
            // A fallback to 0 doesn't make too much sense, but this prevents a crash and empty days will be filtered out later
            val minCapacity = capacitiesFutureDay.minOfOrNull { it.capacity } ?: 0
            val maxCapacity = capacitiesFutureDay.maxOfOrNull { it.capacity } ?: 0
            val dayFullName = previousDay.toLocalDateTime(dutchTimeZone).date.toFullDayOfWeek()
            val contentDescription = getString(Res.string.graph_next_day_content_description, dayFullName, minCapacity, maxCapacity)
            GraphDayModel(
                isToday = false,
                previousDay.toLocalDateTime(dutchTimeZone).date.toNarrowDayOfWeek(),
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

    private suspend fun getGraphToday(
        now: Instant,
        nowInNL: LocalDateTime,
        historicalCapacities: List<CapacityModel>
    ): GraphDayModel {
        val startOfDay = nowInNL.date.atStartOfDayIn(dutchTimeZone)
        val capacitiesToday = historicalCapacities.filter { it.createTime > startOfDay }

        // After the beginning of the next hour, we want to show data from the next week
        val startLastWeek = now.plus(DateTimePeriod(days = -7, hours = 1), dutchTimeZone).toLocalDateTime(dutchTimeZone).truncateToHour()
        val startLastWeekInstant = startLastWeek.toInstant(dutchTimeZone)

        // plusHours(1) will you straight into next day when it's past 23:00
        val endLastWeek = if (startLastWeek.hour == 0) {
            startLastWeekInstant.plus(10, DateTimeUnit.MINUTE)
        } else {
            // We also want the 00:00 hours to complete the day, but that one usually only arrives at something like 00:01
            startLastWeek.atEndOfDayIn(dutchTimeZone).plus(10, DateTimeUnit.MINUTE)
        }
        val capacitiesPrediction = historicalCapacities.filter { it.createTime >= startLastWeekInstant && it.createTime <= endLastWeek }

        // A fallback to 0 doesn't make too much sense, but this prevents a crash and empty days will be filtered out later
        val minCapacityToday = capacitiesToday.minOfOrNull { it.capacity } ?: 0
        val maxCapacityToday = capacitiesToday.maxOfOrNull { it.capacity } ?: 0
        val minCapacityPrediction = capacitiesPrediction.minOfOrNull { it.capacity } ?: 0
        val maxCapacityPrediction = capacitiesPrediction.maxOfOrNull { it.capacity } ?: 0
        val contentDescription = getString(
            Res.string.graph_today_content_description,
            minCapacityToday,
            maxCapacityToday,
            minCapacityPrediction,
            maxCapacityPrediction
        )

        val graphToday = GraphDayModel(
            isToday = true,
            nowInNL.date.toNarrowDayOfWeek(),
            nowInNL.date.toFullDayOfWeek(),
            capacitiesToday,
            capacitiesPrediction,
            contentDescription
        )
        return graphToday
    }

    private fun convertHourlyCapacities(hourlyLocationCapacityDtos: List<HourlyLocationCapacityDto>): List<CapacityModel> {
        return hourlyLocationCapacityDtos.map {
            val firstCapacity = it.document.fields.first.integerValue.toInt()
            val createTime = it.document.createTime
            CapacityModel(
                firstCapacity,
                createTime
            )
        }
    }

    companion object {
        fun getDayName(dayOfWeek: Int): StringResource {
            return when (dayOfWeek) {
                1 -> Res.string.day_1
                2 -> Res.string.day_2
                3 -> Res.string.day_3
                4 -> Res.string.day_4
                5 -> Res.string.day_5
                6 -> Res.string.day_6
                7 -> Res.string.day_7
                else -> throw Exception("Unexpected day of week $dayOfWeek")
            }
        }
    }
}