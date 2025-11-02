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
        return Coordinates(52.09287092917569, 5.111436651758041)
//        val locator = Locator.mobile()
//        return locator.current().coordinates
    }

}