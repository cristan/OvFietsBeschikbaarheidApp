package nl.ovfietsbeschikbaarheid

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
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
            val allLocations = OverviewRepository().getOverviewData()

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

            // TODO: check if we can determine the price
            // TODO: check replacements
        }
    }
}
