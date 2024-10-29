package nl.ovfietsbeschikbaarheid.mapper

import dev.jordond.compass.Coordinates
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.ext.distanceTo
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.model.LocationType
import java.text.DecimalFormat
import kotlin.math.roundToInt

object LocationsMapper {
    fun map(locations: List<LocationDTO>): List<LocationOverviewModel> {
        val replacements = hashMapOf(
            Pair("s-Hertogenbosch", "'s-Hertogenbosch"),
            Pair("Delft, Fietsenstalling", "Delft"),
            Pair("Leiden Centraal,Uitgang LUMC", "Leiden Centraal, Uitgang LUMC"),
            Pair("Vianen OV-fiets", "Vianen"),
            Pair("Hollandse Rading OV-fiets ", "Hollandse Rading"),
            // All the other locations at Utrecht start with the word Utrecht, including the other P+Rs. Use the same scheme to make sure they're sorted together.
            Pair("P + R Utrecht Science Park (De Uithof)", "Utrecht P+R Science Park (De Uithof)"),
            // All of these also help with the alphabetical order
            Pair("OV-ebike Arnhem Centrum", "Arnhem Centrum - OV-ebike"),
            Pair("OV-ebike Driebergen-Zeist", "Driebergen-Zeist - OV-ebike"),
            Pair("OV-ebike Groningen", "Groningen - OV-ebike"),
            Pair("OV-ebike Maastricht", "Maastricht - OV-ebike"),
            Pair("OV-fiets - Maastricht", "Maastricht"),
        )

        return locations.map { toMap ->
            val description = replacements[toMap.description] ?: toMap.description
            LocationOverviewModel(
                title = description,
                rentalBikesAvailable = toMap.extra.rentalBikes,
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
        val kmFormat = DecimalFormat().apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
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
}