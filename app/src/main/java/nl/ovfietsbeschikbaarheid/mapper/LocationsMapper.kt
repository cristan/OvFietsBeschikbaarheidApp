package nl.ovfietsbeschikbaarheid.mapper

import dev.jordond.compass.Coordinates
import nl.ovfietsbeschikbaarheid.dto.LocationsDTO
import nl.ovfietsbeschikbaarheid.ext.distanceTo
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import java.text.DecimalFormat
import kotlin.math.roundToInt

object LocationsMapper {
    private val nonExistingLocations = listOf(
        "asb003",
        "ut018",
        "UTVR002",
        "ehv004",
        "gvc021",
        "had002",
        "ed001",
        "ed002",
    )

    fun map(locationsDTO: LocationsDTO): List<LocationOverviewModel> {
        val locations = locationsDTO.locaties.values
            .filter { !nonExistingLocations.contains(it.extra.locationCode) }

        val replacements = hashMapOf(
            Pair("s-Hertogenbosch", "'s-Hertogenbosch"),
            Pair("P + R Utrecht Science Park (De Uithof)", "Utrecht P+R Science Park (De Uithof)"),
            Pair("Delft, Fietsenstalling", "Delft"),
        )

        return locations.map { toMap ->
            val description = replacements[toMap.description] ?: toMap.description
            LocationOverviewModel(
                title = description.trim().replace("P + R Utrecht Science Park (De Uithof)", "Utrecht P+R Science Park (De Uithof)"),
                uri = toMap.link.uri,
                locationCode = toMap.extra.locationCode,
                stationCode = toMap.stationCode,
                latitude = toMap.lat,
                longitude = toMap.lng,
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