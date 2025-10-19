package nl.ovfietsbeschikbaarheid.util

import dev.jordond.compass.Coordinates

interface LocationLoader {
    suspend fun getLastKnownCoordinates(): Coordinates?

    suspend fun loadCurrentCoordinates(): Coordinates?
}