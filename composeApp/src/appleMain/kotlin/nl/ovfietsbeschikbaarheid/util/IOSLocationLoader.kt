package nl.ovfietsbeschikbaarheid.util

import dev.jordond.compass.Coordinates
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.exception.GeolocationException

class IOSLocationLoader(private val locator: Locator): LocationLoader {

    override suspend fun getLastKnownCoordinates(): Coordinates? {
        return locator.lastLocation()?.coordinates
    }

    override suspend fun loadCurrentCoordinates(): Coordinates? {
        return try {
            locator.current().coordinates
        } catch (_: GeolocationException) {
            // Compass doesn't return null when a location can't be determined, instead it crashes with a GeolocationException
            //
            // This is probably to differentiate between:
            // * Getting the location isn't supported on this device (NotSupportedException, only happens on web)
            // * The location permission isn't granted (PermissionException)
            // * There was a problem getting the location (GeolocationException, and not getting any location counts as well)
            null
        }
    }

}