package nl.ovfietsbeschikbaarheid.ext

import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.model.ServiceType
import timber.log.Timber
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun List<LocationDTO>.getMinutesSinceLastUpdate(now: Instant): Long {
    val lastUpdateTimestamp = this.maxOf { it.extra.fetchTime }
    val lastUpdateInstant = Instant.fromEpochSeconds(lastUpdateTimestamp)
    return (now - lastUpdateInstant).inWholeMinutes
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