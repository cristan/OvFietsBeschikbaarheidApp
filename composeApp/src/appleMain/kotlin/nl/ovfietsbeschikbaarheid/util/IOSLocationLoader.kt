package nl.ovfietsbeschikbaarheid.util

import dev.jordond.compass.Coordinates

class IOSLocationLoader: LocationLoader {
    override suspend fun getLastKnownCoordinates(): Coordinates? {
        // TODO: replace with real implementation
        return Coordinates(latitude = 52.090746, longitude = 5.110702)
    }

    override suspend fun loadCurrentCoordinates(): Coordinates? {
        // TODO: replace with real implementation
        return Coordinates(latitude = 52.090746, longitude = 5.110702)
    }

}