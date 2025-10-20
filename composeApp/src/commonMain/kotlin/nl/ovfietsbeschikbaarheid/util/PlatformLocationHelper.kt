package nl.ovfietsbeschikbaarheid.util

interface PlatformLocationHelper {
    fun isGpsTurnedOn(): Boolean

    fun turnOnGps()

    fun shouldShowLocationRationale(): Boolean

    fun isDeniedPermanently(): Boolean
}