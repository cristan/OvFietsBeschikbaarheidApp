package nl.ovfietsbeschikbaarheid

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class LocationsDataTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val stationRepository = StationRepository(context)
        val allStations = stationRepository.getAllStations()
        runBlocking {
            val httpClient = KtorApiClient()
            val locations = httpClient.getLocations()
            val allLocations = LocationsMapper.map(locations)
            val pricePer24Hours = LocationsMapper.getPricePer24Hours(locations)

            val lastUpdateTimestamp = allLocations.maxOf { it.fetchTime }
            val lastUpdateInstant = Instant.ofEpochSecond(lastUpdateTimestamp)
            val lastUpdateAgo = lastUpdateInstant.until(Instant.now(), ChronoUnit.MINUTES)
            println("The last update (at $lastUpdateTimestamp) was $lastUpdateAgo minutes ago")
            if(lastUpdateAgo > 10) {
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

            LocationsMapper.replacements.keys.forEach { replacementOriginal ->
                if(!locations.any { it.description == replacementOriginal }) {
                    error("Replacement $replacementOriginal not found")
                }
            }
        }
    }
}
