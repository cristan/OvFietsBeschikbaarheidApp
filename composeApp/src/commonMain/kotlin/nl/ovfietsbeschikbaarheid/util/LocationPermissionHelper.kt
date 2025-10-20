package nl.ovfietsbeschikbaarheid.util

import dev.jordond.compass.Priority
import dev.jordond.compass.permissions.LocationPermissionController
import dev.jordond.compass.permissions.PermissionState
import dev.jordond.compass.permissions.mobile
import dev.jordond.compass.permissions.mobile.openSettings

class LocationPermissionHelper(
    private val platformLocationHelper: PlatformLocationHelper
){
    fun shouldShowLocationRationale(): Boolean = platformLocationHelper.shouldShowLocationRationale()

    fun isDeniedPermanently(): Boolean = platformLocationHelper.isDeniedPermanently()

    fun isGpsTurnedOn(): Boolean = platformLocationHelper.isGpsTurnedOn()

    fun turnOnGps() = platformLocationHelper.turnOnGps()

    fun hasGpsPermission() = LocationPermissionController.mobile().hasPermission()

    fun openSettings() = LocationPermissionController.openSettings()

    suspend fun requirePermission(): PermissionState = LocationPermissionController.mobile().requirePermissionFor(Priority.Balanced)
}