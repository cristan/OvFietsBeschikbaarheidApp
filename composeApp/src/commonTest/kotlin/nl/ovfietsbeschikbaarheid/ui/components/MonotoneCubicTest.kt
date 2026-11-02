package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.ui.geometry.Offset
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import nl.ovfietsbeschikbaarheid.ext.dutchTimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class MonotoneCubicTest {

    // Utrecht Centraal Jaarbeursplein on Monday 19 October 2026: it ran out of OV-fietsen between 11:00 and 13:00 Dutch time,
    // so the capacity drops steeply onto a flat stretch at 0
    private val capacities = listOf(
        "2026-10-18T22:00:14.738738Z" to 470,
        "2026-10-18T23:00:18.282506Z" to 470,
        "2026-10-19T00:00:22.324768Z" to 468,
        "2026-10-19T01:00:26.248448Z" to 468,
        "2026-10-19T02:00:30.904389Z" to 466,
        "2026-10-19T03:00:38.065436Z" to 467,
        "2026-10-19T04:00:42.703159Z" to 468,
        "2026-10-19T05:00:44.966221Z" to 460,
        "2026-10-19T06:00:49.914017Z" to 361,
        "2026-10-19T07:00:52.200391Z" to 68,
        "2026-10-19T08:00:55.572097Z" to 2,
        "2026-10-19T09:00:06.792732Z" to 0,
        "2026-10-19T10:00:11.258032Z" to 0,
        "2026-10-19T11:00:15.070887Z" to 0,
        "2026-10-19T12:00:17.925123Z" to 3,
        "2026-10-19T13:00:22.012481Z" to 15,
        "2026-10-19T14:00:26.144071Z" to 68,
        "2026-10-19T15:00:29.651165Z" to 228,
        "2026-10-19T16:00:31.369815Z" to 415,
        "2026-10-19T17:00:33.524717Z" to 444,
        "2026-10-19T18:00:35.892284Z" to 450,
        "2026-10-19T19:00:41.825050Z" to 457,
        "2026-10-19T20:02:35.086591Z" to 485,
        "2026-10-19T21:00:46.047165Z" to 495,
        "2026-10-19T22:00:48.361791Z" to 498,
    )

    private val midnight = LocalDateTime(2026, 10, 19, 0, 0).toInstant(dutchTimeZone)

    private val points = capacities.map { (createTime, capacity) ->
        val hoursSinceMidnight = (Instant.parse(createTime) - midnight).inWholeSeconds / 3600f
        Offset(hoursSinceMidnight, capacity.toFloat())
    }

    private val segments = monotoneCubicSegments(points)

    @Test
    fun `passes through every capacity`() {
        assertEquals(points.drop(1), segments.map { it.end })
    }

    @Test
    fun `never leaves the range of the two capacities it connects`() {
        segments.forEachIndexed { index, segment ->
            val start = points[index]
            val end = segment.end
            listOf(segment.control1, segment.control2).forEach { control ->
                // A Bézier curve stays within the bounds of its control points
                assertTrue(control.y in minOf(start.y, end.y)..maxOf(start.y, end.y), "$control lies outside the capacities of $start and $end")
            }
        }
    }

    @Test
    fun `has no corners`() {
        segments.zipWithNext { incoming, outgoing ->
            val point = incoming.end
            val incomingSlope = (point.y - incoming.control2.y) / (point.x - incoming.control2.x)
            val outgoingSlope = (outgoing.control1.y - point.y) / (outgoing.control1.x - point.x)
            assertEquals(incomingSlope, outgoingSlope, absoluteTolerance = 0.001f, "Corner at $point")
        }
    }
}
