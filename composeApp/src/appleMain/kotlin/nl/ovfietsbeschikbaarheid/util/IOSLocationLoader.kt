package nl.ovfietsbeschikbaarheid.util

import dev.jordond.compass.Coordinates
import dev.jordond.compass.geolocation.Locator

class IOSLocationLoader(private val locator: Locator): LocationLoader {

    override suspend fun getLastKnownCoordinates(): Coordinates? {
        return locator.lastLocation()?.coordinates
    }

    override suspend fun loadCurrentCoordinates(): Coordinates? {
        return locator.current().coordinates
    }

}