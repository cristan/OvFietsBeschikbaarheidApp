package nl.ovfietsbeschikbaarheid

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.LocationHistoryDTO
import nl.ovfietsbeschikbaarheid.ext.dutchTimeZone
import nl.ovfietsbeschikbaarheid.mapper.CapacityHistoryMapper
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.resources.Res
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Stands in for the history files until the backend publishes them.
 * bundled_history.json holds every change the backend saw from Wednesday 21 October 2026 23:11 until Friday 01:04 Dutch time.
 * Each of the last 8 days repeats that Thursday, starting with the number of rental bikes it had at midnight.
 */
@OptIn(ExperimentalTime::class)
object BundledHistory {
    private val thursday = LocalDate(2026, 10, 22)

    suspend fun getLocationHistory(locationCode: String, now: Instant): LocationHistoryDTO? {
        val histories = Json.decodeFromString<Map<String, LocationHistoryDTO>>(Res.readBytes("files/bundled_history.json").decodeToString())
        val changes = CapacityHistoryMapper.toCapacityChanges(histories[locationCode] ?: return null, null)
        val startOfThursday = thursday.atStartOfDayIn(dutchTimeZone)
        val thursdayChanges = changes.filter { it.createTime > startOfThursday && it.createTime.toLocalDateTime(dutchTimeZone).date == thursday }
        val capacityAtStartOfThursday = changes.lastOrNull { it.createTime <= startOfThursday }?.capacity ?: thursdayChanges.first().capacity

        val today = now.toLocalDateTime(dutchTimeZone).date
        val days = (7 downTo 0).map { today.minus(it, DateTimeUnit.DAY) }
        val repeatedChanges = days.flatMap { day ->
            listOf(CapacityModel(capacityAtStartOfThursday, day.atStartOfDayIn(dutchTimeZone))) +
                thursdayChanges.map { change ->
                    val time = change.createTime.toLocalDateTime(dutchTimeZone).time
                    CapacityModel(change.capacity, LocalDateTime(day, time).toInstant(dutchTimeZone))
                }
        }.filter { it.createTime <= now }

        val minutes = repeatedChanges.map { it.createTime.epochSeconds / 60 }
        val capacities = repeatedChanges.map { it.capacity }
        return LocationHistoryDTO(
            minuteDeltas = minutes.zip(listOf(0L) + minutes) { minute, previousMinute -> minute - previousMinute },
            rentalBikeDeltas = capacities.zip(listOf(0) + capacities) { capacity, previousCapacity -> capacity - previousCapacity },
        )
    }
}
