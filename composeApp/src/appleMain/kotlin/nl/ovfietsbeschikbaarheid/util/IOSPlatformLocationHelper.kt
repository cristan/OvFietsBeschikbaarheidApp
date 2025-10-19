package nl.ovfietsbeschikbaarheid.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
class IOSPlatformLocationHelper : PlatformLocationHelper {
    
    private val locationManager = CLLocationManager()

    override fun isGpsTurnedOn(): Boolean {
        return CLLocationManager.locationServicesEnabled()
    }

    override fun turnOnGps() {
        // Open iOS Settings app to location settings
        val settingsUrl = NSURL.URLWithString("App-Prefs:Privacy&path=LOCATION")
        settingsUrl?.let { url ->
            UIApplication.sharedApplication.openURL(url)
        }
    }

    override fun shouldShowLocationRationale(): Boolean {
        // On iOS, we should show rationale if permission was denied
        // but not if it was never requested
        return locationManager.authorizationStatus == kCLAuthorizationStatusDenied
    }
}