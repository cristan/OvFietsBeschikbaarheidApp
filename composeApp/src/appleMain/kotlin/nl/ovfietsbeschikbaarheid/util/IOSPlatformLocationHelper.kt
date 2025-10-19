package nl.ovfietsbeschikbaarheid.util

// TODO: add real implementations
class IOSPlatformLocationHelper: PlatformLocationHelper {
    override fun isGpsTurnedOn(): Boolean {
        return true
    }

    override fun turnOnGps() {
        Unit
    }

    override fun shouldShowLocationRationale(): Boolean {
        return false
    }

}