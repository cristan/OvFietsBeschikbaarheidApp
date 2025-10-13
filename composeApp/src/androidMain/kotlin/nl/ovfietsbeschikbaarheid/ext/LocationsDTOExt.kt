package nl.ovfietsbeschikbaarheid.ext

import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.model.ServiceType
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit

fun List<LocationDTO>.getMinutesSinceLastUpdate(now: Instant): Long {
    val lastUpdateTimestamp = this.maxOf { it.extra.fetchTime }
    val lastUpdateInstant = Instant.ofEpochSecond(lastUpdateTimestamp)
    return lastUpdateInstant.until(now, ChronoUnit.MINUTES)
}

fun LocationDTO.getServiceType(): ServiceType? {
    return when (extra.serviceType) {
        "Bemenst" -> ServiceType.Bemenst
        "Kluizen" -> ServiceType.Kluizen
        "Sleutelautomaat" -> ServiceType.Sleutelautomaat
        "Box" -> ServiceType.Box
        null -> if (link.uri.contains("Zelfservice", ignoreCase = true)) ServiceType.Zelfservice else null
        else -> {
            Timber.w("Unknown service type: ${extra.serviceType}")
            null
        }
    }
}