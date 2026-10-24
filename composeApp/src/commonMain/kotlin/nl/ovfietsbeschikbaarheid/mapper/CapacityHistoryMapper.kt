package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.dto.LocationHistoryDTO
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object CapacityHistoryMapper {
    fun toCapacityChanges(locationHistory: LocationHistoryDTO?, recentLocationHistory: LocationHistoryDTO?): List<CapacityModel> {
        val historyChanges = locationHistory?.decodeCapacityChanges().orEmpty()
        val recentChanges = recentLocationHistory?.decodeCapacityChanges().orEmpty()
        val firstRecentChange = recentChanges.firstOrNull() ?: return historyChanges
        return historyChanges.filter { it.createTime < firstRecentChange.createTime } + recentChanges
    }

    fun graphPointsBetween(capacityChanges: List<CapacityModel>, start: Instant, end: Instant): List<CapacityModel> {
        if (start >= end) return emptyList()
        val changeInEffectAtStart = capacityChanges.lastOrNull { it.createTime <= start }?.copy(createTime = start)
        val changesInWindow = listOfNotNull(changeInEffectAtStart) + capacityChanges.filter { it.createTime > start && it.createTime <= end }
        val lastChange = changesInWindow.lastOrNull() ?: return emptyList()
        // A capacity holds until the next change, so it is repeated a minute before each change and at the end
        val steps = changesInWindow.zipWithNext { previous, next ->
            val previousCapacityUntilChange = CapacityModel(previous.capacity, next.createTime - 1.minutes)
            listOfNotNull(previousCapacityUntilChange.takeIf { it.createTime > previous.createTime }, next)
        }.flatten()
        val lastCapacityUntilEnd = CapacityModel(lastChange.capacity, end).takeIf { lastChange.createTime < end }
        return listOf(changesInWindow.first()) + steps + listOfNotNull(lastCapacityUntilEnd)
    }

    private fun LocationHistoryDTO.decodeCapacityChanges(): List<CapacityModel> {
        val epochMinutes = minuteDeltas.runningReduce(Long::plus)
        val rentalBikes = rentalBikeDeltas.runningReduce(Int::plus)
        return epochMinutes.zip(rentalBikes) { epochMinute, capacity -> CapacityModel(capacity, Instant.fromEpochSeconds(epochMinute * 60)) }
    }
}
