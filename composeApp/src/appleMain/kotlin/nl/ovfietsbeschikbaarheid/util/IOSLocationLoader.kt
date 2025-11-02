package nl.ovfietsbeschikbaarheid.util

import dev.jordond.compass.Coordinates
import dev.jordond.compass.geolocation.Locator

class IOSLocationLoader(private val locator: Locator): LocationLoader {

    override suspend fun getLastKnownCoordinates(): Coordinates? {
        return locator.lastLocation()?.coordinates
    }

    override suspend fun loadCurrentCoordinates(): Coordinates? {
        return Coordinates(52.09287092917569, 5.111436651758041)
        //return locator.current().coordinates
    }

}