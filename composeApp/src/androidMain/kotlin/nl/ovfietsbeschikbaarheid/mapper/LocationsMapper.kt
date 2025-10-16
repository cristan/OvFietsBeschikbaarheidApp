package nl.ovfietsbeschikbaarheid.mapper

import dev.jordond.compass.Coordinates
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.ext.distanceTo
import nl.ovfietsbeschikbaarheid.ext.getMinutesSinceLastUpdate
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.model.LocationType
import nl.ovfietsbeschikbaarheid.util.dutchLocale
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object LocationsMapper {
    val replacements = hashMapOf(
        Pair("s-Hertogenbosch", "'s-Hertogenbosch"),
        Pair("Delft, Fietsenstalling", "Delft"),
        Pair("Leiden Centraal,Uitgang LUMC", "Leiden Centraal, Uitgang LUMC"),

        Pair("Hollandse Rading OV-fiets", "Hollandse Rading"),
        Pair("Vianen OV-fiets", "Vianen"),
        Pair("OV-fiets - Maastricht", "Maastricht"),
        Pair("OV-fiets Kesteren", "Kesteren"),
        Pair("OV-fiets Den Haag Ypenburg", "Den Haag Ypenburg"),

        Pair("Openbare fietsenstalling gemeente Groningen : Fietsenstalling Europapark", "Groningen Europapark"),
        Pair("Gilze Rijen", "Gilze-Rijen"),

        // All of these also help with the alphabetical order
        Pair("OV-ebike Arnhem Centrum", "Arnhem Centrum - OV-ebike"),
        Pair("OV-ebike Driebergen-Zeist", "Driebergen-Zeist - OV-ebike"),
        Pair("OV-ebike Groningen", "Groningen - OV-ebike"),
        Pair("OV-ebike Maastricht", "Maastricht - OV-ebike"),
    )

    @OptIn(ExperimentalTime::class)
    fun map(locations: List<LocationDTO>): List<LocationOverviewModel> {
        val minutesSinceLastUpdate = locations.getMinutesSinceLastUpdate(Clock.System.now())
        val lastUpdateTooLongAgo = minutesSinceLastUpdate > 15
        if (lastUpdateTooLongAgo) {
            Timber.e("The last update was $minutesSinceLastUpdate minutes ago")
        }

        return locations.map { toMap ->
            val description = replacements[toMap.description] ?: toMap.description
            LocationOverviewModel(
                title = description,
                rentalBikesAvailable = if (lastUpdateTooLongAgo) null else toMap.extra.rentalBikes,
                uri = toMap.link.uri,
                fetchTime = toMap.extra.fetchTime,
                locationCode = toMap.extra.locationCode,
                stationCode = toMap.stationCode,
                latitude = toMap.lat,
                longitude = toMap.lng,
                type = if (description.contains("OV-ebike")) LocationType.EBike else LocationType.Regular,
                openingHours = toMap.openingHours
            )
        }.sortedBy { it.title }
    }

    fun withDistance(locations: List<LocationOverviewModel>, currentCoordinates: Coordinates): List<LocationOverviewWithDistanceModel> {
        val symbols = DecimalFormatSymbols(dutchLocale)

        val kmFormat = DecimalFormat().apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
            decimalFormatSymbols = symbols
        }
        return locations
            .sortedBy { it.distanceTo(currentCoordinates) }
            .map {
                val distance = it.distanceTo(currentCoordinates)
                val formattedDistance = if (distance < 1000) {
                    "${distance.roundToInt()} m"
                } else {
                    "${kmFormat.format(distance / 1000)} km"
                }
                LocationOverviewWithDistanceModel(formattedDistance, it)
            }
    }

    fun getPricePer24Hours(locations: List<LocationDTO>): String? {
        val regex = Regex(".*Je betaalt € ([\\d,]+) per 24 uur\\..*")
        return locations
            .asSequence()
            .flatMap { it.infoImages.asSequence() }
            .map { it.body }
            .mapNotNull { regex.find(it)?.groupValues?.get(1) }
            .firstOrNull()
    }
}