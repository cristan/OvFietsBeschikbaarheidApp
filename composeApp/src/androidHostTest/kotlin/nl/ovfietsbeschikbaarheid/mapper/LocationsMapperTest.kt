package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.testutils.locationsResponse
import nl.ovfietsbeschikbaarheid.testutils.shouldBeEqualTo
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import org.junit.Test

class LocationsMapperTest {

    @Test
    fun `mapping without rental bikes available leaves every amount empty`() {
        // Every location in this response has an amount of rental bikes
        val locations = LocationsMapper(DecimalFormatter()).mapWithoutRentalBikesAvailable(locationsResponse)

        locations.size shouldBeEqualTo 283
        locations.count { it.rentalBikesAvailable != null } shouldBeEqualTo 0
    }
}
