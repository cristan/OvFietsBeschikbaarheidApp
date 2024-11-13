package nl.ovfietsbeschikbaarheid

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.util.dutchLocale
import org.junit.Test

class LocationsDataTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val stationRepository = StationRepository(context)
        val allStations = stationRepository.getAllStations()
        val capacities = stationRepository.getCapacities()
        runBlocking {
            val allLocations = OverviewRepository().getAllLocations()
            allLocations.forEach {
                val stationName = allStations[it.stationCode]
                if (stationName == null && it.stationCode != "BSLC") {
                    // Triggers at Eindhoven Strijp-S. This is a "new" train station since 2015, so probably from just after stations_nl_2015_08 is made.
                    // Same applies to Utrecht Vaartsche Rijn: this is made at 2016.
                    println("Station ${it.stationCode} not found for $it")
                }
                val foundCapacity = capacities[it.locationCode.lowercase(dutchLocale)]
                if (foundCapacity == null) {
                    error("Capacity for ${it.locationCode} not found")
                }
            }

        }
    }
}
