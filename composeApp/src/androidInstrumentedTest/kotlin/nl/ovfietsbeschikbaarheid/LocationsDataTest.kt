package nl.ovfietsbeschikbaarheid

import kotlinx.coroutines.runBlocking
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class LocationsDataTest {

    @Test
    fun test() {
        val stationRepository = StationRepository()
        val locationsMapper = LocationsMapper(DecimalFormatter())
        runBlocking {
            val allStations = stationRepository.getAllStations()
            val httpClient = KtorApiClient()
            val locations = httpClient.getLocations()
            val allLocations = locationsMapper.map(locations)
            val pricePer24Hours = locationsMapper.getPricePer24Hours(locations)

            val lastUpdateTimestamp = allLocations.maxOf { it.fetchTime }
            val lastUpdateInstant = Instant.fromEpochSeconds(lastUpdateTimestamp)
            val lastUpdateAgo = (Clock.System.now() - lastUpdateInstant).inWholeMinutes
            println("The last update (at $lastUpdateTimestamp) was $lastUpdateAgo minutes ago")
            if (lastUpdateAgo > 10) {
                error("The last update (at $lastUpdateTimestamp) was $lastUpdateAgo minutes ago")
            }

            // Check if we have a station name for all of the locations
            allLocations.forEach {
                val stationName = allStations[it.stationCode]
                if (stationName == null && it.stationCode != "BSLC") {
                    error("Station ${it.stationCode} not found for $it")
                }
            }

            if (pricePer24Hours == null) {
                error("Could not determine the price per 24 hours!")
            }

            locationsMapper.replacements.keys.forEach { replacementOriginal ->
                if (!locations.any { it.description == replacementOriginal }) {
                    error("Replacement $replacementOriginal not found")
                }
            }
        }
    }
}
