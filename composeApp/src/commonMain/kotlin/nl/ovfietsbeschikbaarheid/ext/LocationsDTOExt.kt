package nl.ovfietsbeschikbaarheid.ext

import co.touchlab.kermit.Logger
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.model.ServiceType
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
            Logger.w("Unknown service type: ${extra.serviceType}")
            null
        }
    }
}

fun LocationDTO.getDirections(): String? {
    val directions = infoImages.find { it.title == "Routebeschrijving" }?.body?.trimEnd()
    return if (directions != "") directions else null
}

fun LocationDTO.getAbout(): String? = infoImages.find { it.title == "Bijzonderheden" }?.body?.trimEnd()

// Filled in for example at Leiden Centraal, Centrumzijde
fun LocationDTO.getOpeningHoursInfo(): String? = infoImages.find { it.title == "Info openingstijden" }?.body

fun LocationDTO.getDisruptions(): String? = infoImages.find { it.title == "Storing" }?.body

fun LocationDTO.isAlternativeTo(otherStationCode: String, otherLocationCode: String): Boolean {
    return otherStationCode == stationCode &&

            // Except BSLC. This isn't a station, these are self service stations
            otherStationCode != "BSLC" &&

            otherLocationCode != extra.locationCode
}
