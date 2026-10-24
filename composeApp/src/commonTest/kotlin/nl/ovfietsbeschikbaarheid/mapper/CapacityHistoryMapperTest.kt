package nl.ovfietsbeschikbaarheid.mapper

import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.LocationHistoryDTO
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class CapacityHistoryMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Raalte on Thursday 22 October 2026 at 19:10 Dutch time: it ran out of OV-fietsen from 12:33 until 18:52
    private val locationHistory = json.decodeFromString<LocationHistoryDTO>(
        """{"minuteDeltas":[29876951,497,214,15,76,379],"rentalBikeDeltas":[10,-1,-1,-4,-4,6]}"""
    )
    private val recentLocationHistory = json.decodeFromString<Map<String, LocationHistoryDTO>>(
        """{"rat001":{"minuteDeltas":[29877753,379,2,2,8],"rentalBikeDeltas":[0,6,1,2,1]}}"""
    ).getValue("rat001")

    private val capacityChanges = listOf(
        CapacityModel(10, Instant.parse("2026-10-21T21:11:00Z")),
        CapacityModel(9, Instant.parse("2026-10-22T05:28:00Z")),
        CapacityModel(8, Instant.parse("2026-10-22T09:02:00Z")),
        CapacityModel(4, Instant.parse("2026-10-22T09:17:00Z")),
        CapacityModel(0, Instant.parse("2026-10-22T10:33:00Z")),
        CapacityModel(6, Instant.parse("2026-10-22T16:52:00Z")),
        CapacityModel(7, Instant.parse("2026-10-22T16:54:00Z")),
        CapacityModel(9, Instant.parse("2026-10-22T16:56:00Z")),
        CapacityModel(10, Instant.parse("2026-10-22T17:04:00Z")),
    )

    @Test
    fun `continues the location history with the recent history`() {
        assertEquals(capacityChanges, CapacityHistoryMapper.toCapacityChanges(locationHistory, recentLocationHistory))
    }

    @Test
    fun `uses only the recent history when the location history is missing`() {
        assertEquals(
            listOf(
                CapacityModel(0, Instant.parse("2026-10-22T10:33:00Z")),
                CapacityModel(6, Instant.parse("2026-10-22T16:52:00Z")),
                CapacityModel(7, Instant.parse("2026-10-22T16:54:00Z")),
                CapacityModel(9, Instant.parse("2026-10-22T16:56:00Z")),
                CapacityModel(10, Instant.parse("2026-10-22T17:04:00Z")),
            ),
            CapacityHistoryMapper.toCapacityChanges(null, recentLocationHistory)
        )
    }

    @Test
    fun `keeps every capacity until a minute before it changes`() {
        val startOfThursday = Instant.parse("2026-10-21T22:00:00Z")
        val endOfThursday = Instant.parse("2026-10-22T21:59:59Z")

        assertEquals(
            listOf(
                CapacityModel(10, Instant.parse("2026-10-21T22:00:00Z")),
                CapacityModel(10, Instant.parse("2026-10-22T05:27:00Z")),
                CapacityModel(9, Instant.parse("2026-10-22T05:28:00Z")),
                CapacityModel(9, Instant.parse("2026-10-22T09:01:00Z")),
                CapacityModel(8, Instant.parse("2026-10-22T09:02:00Z")),
                CapacityModel(8, Instant.parse("2026-10-22T09:16:00Z")),
                CapacityModel(4, Instant.parse("2026-10-22T09:17:00Z")),
                CapacityModel(4, Instant.parse("2026-10-22T10:32:00Z")),
                CapacityModel(0, Instant.parse("2026-10-22T10:33:00Z")),
                CapacityModel(0, Instant.parse("2026-10-22T16:51:00Z")),
                CapacityModel(6, Instant.parse("2026-10-22T16:52:00Z")),
                CapacityModel(6, Instant.parse("2026-10-22T16:53:00Z")),
                CapacityModel(7, Instant.parse("2026-10-22T16:54:00Z")),
                CapacityModel(7, Instant.parse("2026-10-22T16:55:00Z")),
                CapacityModel(9, Instant.parse("2026-10-22T16:56:00Z")),
                CapacityModel(9, Instant.parse("2026-10-22T17:03:00Z")),
                CapacityModel(10, Instant.parse("2026-10-22T17:04:00Z")),
                CapacityModel(10, Instant.parse("2026-10-22T21:59:59Z")),
            ),
            CapacityHistoryMapper.graphPointsBetween(capacityChanges, startOfThursday, endOfThursday)
        )
    }

    @Test
    fun `has no graph points before the history starts`() {
        val startOfTuesday = Instant.parse("2026-10-19T22:00:00Z")
        val endOfTuesday = Instant.parse("2026-10-20T21:59:59Z")

        assertEquals(emptyList(), CapacityHistoryMapper.graphPointsBetween(capacityChanges, startOfTuesday, endOfTuesday))
    }

    @Test
    fun `has no graph points when the end comes before the start`() {
        // The rest of last Thursday, seen from Thursday 29 October 2026 at 23:30: it starts at the next full hour
        val startOfFriday = Instant.parse("2026-10-22T22:00:00Z")
        val endOfThursday = Instant.parse("2026-10-22T21:59:59Z")

        assertEquals(emptyList(), CapacityHistoryMapper.graphPointsBetween(capacityChanges, startOfFriday, endOfThursday))
    }
}
