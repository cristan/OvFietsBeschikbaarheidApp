package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.ext.getMinutesSinceLastUpdate
import timber.log.Timber
import java.time.Instant

class DetailsRepository(
    private val client: KtorApiClient,
) {

    suspend fun getDetails(locationCode: String): LocationDTO? {
        val allLocations = client.getLocations()
        val location = allLocations.find { it.extra.locationCode == locationCode }
        if (location == null) {
            // Should be super duper rare, only when the location happens to disappear from the list in the meantime between loading the overview and going to the details
            Timber.w("Location with code $locationCode not found!")
            return null
        }

        val minutesSinceLastUpdate = allLocations.getMinutesSinceLastUpdate(Instant.now())
        val lastUpdateTooLongAgo = minutesSinceLastUpdate > 15
        if (lastUpdateTooLongAgo) {
            Timber.e("The last update was $minutesSinceLastUpdate minutes ago")
        }

        if (location.extra.rentalBikes == null || lastUpdateTooLongAgo) {
            val nsApiDetails = client.getNSApiDetails(location.link.uri)
            return location.copy(
                extra = location.extra.copy(
                    rentalBikes = nsApiDetails?.payload?.extra?.rentalBikes,
                    // The NS backend doesn't have a fetchTime, so assume right now
                    fetchTime = System.currentTimeMillis()
                )
            )
        }
        return location
    }
}