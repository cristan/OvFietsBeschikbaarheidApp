package nl.ovfietsbeschikbaarheid.util

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
class IOSPlatformLocationHelper : PlatformLocationHelper {
    
    private val locationManager = CLLocationManager()

    override fun isGpsTurnedOn(): Boolean {
        val locationServicesEnabled = CLLocationManager.locationServicesEnabled()
        return locationServicesEnabled
    }

    override fun turnOnGps() {
        // Open iOS Settings app to location settings
        val settingsUrl = NSURL.URLWithString("App-Prefs:Privacy&path=LOCATION")
        settingsUrl?.let { url ->
            UIApplication.sharedApplication.openURL(url)
        }
    }

    override fun shouldShowLocationRationale() = isGpsAuthorizationDenied()

    // On iOS, there is no distinction between denied and denied permanently like in iOS.
    // Once you asked and the user said no, there's nothing we can do except send them to the settings.
    override fun isDeniedPermanently(): Boolean = isGpsAuthorizationDenied()

    private fun isGpsAuthorizationDenied(): Boolean {
        val authorizationStatus = locationManager.authorizationStatus

        when (authorizationStatus) {
            kCLAuthorizationStatusNotDetermined -> Logger.d("Authorization status: Not Determined")
            kCLAuthorizationStatusRestricted -> Logger.d("Authorization status: Restricted")
            kCLAuthorizationStatusDenied -> Logger.d("Authorization status: Denied")
            kCLAuthorizationStatusAuthorizedAlways -> Logger.d("Authorization status: Authorized Always")
            kCLAuthorizationStatusAuthorizedWhenInUse -> Logger.d("Authorization status: Authorized When In Use")
            else -> Logger.d("Authorization status: Unknown ($authorizationStatus)")
        }

        return authorizationStatus == kCLAuthorizationStatusDenied
    }
}