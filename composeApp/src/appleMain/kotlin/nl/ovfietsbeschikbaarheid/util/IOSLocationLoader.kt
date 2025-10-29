package nl.ovfietsbeschikbaarheid.util

import dev.jordond.compass.Coordinates
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.mobile

class IOSLocationLoader: LocationLoader {
    override suspend fun getLastKnownCoordinates(): Coordinates? {
        val locator = Locator.mobile()
        return locator.lastLocation()?.coordinates
    }

    override suspend fun loadCurrentCoordinates(): Coordinates? {
        val locator = Locator.mobile()
        return locator.current().coordinates
    }

}