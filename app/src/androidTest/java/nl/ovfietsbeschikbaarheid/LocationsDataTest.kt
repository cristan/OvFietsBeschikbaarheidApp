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

            // Check if we have a station name for all of the locations
            allLocations.forEach {
                val stationName = allStations[it.stationCode]
                if (stationName == null && it.stationCode != "BSLC") {
                    error("Station ${it.stationCode} not found for $it")
                }
            }
            allLocations.forEach {
                // Check if we have a capacity for each of the locations
                val foundCapacity = capacities[it.locationCode.lowercase(dutchLocale)]
                if (foundCapacity == null) {
                    error("Capacity for ${it.locationCode} not found")
                }

                // Check if the rentalBikesAvailable is higher than the capacity. If yes, the capacities need to be updated
                val rentalBikesAvailable = it.rentalBikesAvailable
                if (rentalBikesAvailable != null && rentalBikesAvailable > foundCapacity) {
                    println("Rental bikes available ($rentalBikesAvailable) is higher than capacity ($foundCapacity) for ${it.locationCode} (${it.title})")
                }
            }
        }
    }
}
