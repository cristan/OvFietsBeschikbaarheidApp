package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.testutils.locationsResponse
import nl.ovfietsbeschikbaarheid.testutils.shouldBeEqualTo
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import org.junit.Test

class LocationsMapperTest {

    @Test
    fun `mapping without capacities drops the capacity of every location`() {
        // Every location in this response has a capacity
        val locations = LocationsMapper(DecimalFormatter()).mapWithoutCapacities(locationsResponse)

        locations.size shouldBeEqualTo 283
        locations.count { it.rentalBikesAvailable != null } shouldBeEqualTo 0
    }
}
