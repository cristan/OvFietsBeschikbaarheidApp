package nl.ovfietsbeschikbaarheid.ext

import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import java.time.Instant
import java.time.temporal.ChronoUnit

fun List<LocationDTO>.getMinutesSinceLastUpdate(now: Instant): Long {
    val lastUpdateTimestamp = this.maxOf { it.extra.fetchTime }
    val lastUpdateInstant = Instant.ofEpochSecond(lastUpdateTimestamp)
    return lastUpdateInstant.until(now, ChronoUnit.MINUTES)
}