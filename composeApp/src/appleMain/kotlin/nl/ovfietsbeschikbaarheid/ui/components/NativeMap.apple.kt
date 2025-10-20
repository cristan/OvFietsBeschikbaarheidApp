package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeMap(modifier: Modifier, latitude: Double, longitude: Double, description: String, rentalBikesAvailable: Int?) {
    val clLocation = CLLocationCoordinate2DMake(
        latitude,
        longitude
    )

    UIKitView(
        modifier = modifier.fillMaxSize(),
        factory = {
            MKMapView().apply {
                setZoomEnabled(true)
                setScrollEnabled(true)
            }
        },
        update = { mapView ->
            // Remove existing annotations before adding new ones
            mapView.removeAnnotations(mapView.annotations)

            val pointAnnotation = MKPointAnnotation().apply {
                setCoordinate(clLocation)
            }
            val region = MKCoordinateRegionMake(
                clLocation,
                MKCoordinateSpanMake(0.01, 0.01)
            )
            mapView.setRegion(region, animated = true)
            mapView.addAnnotation(pointAnnotation)
            mapView.setCenterCoordinate(clLocation, animated = true)
        }
    )
}