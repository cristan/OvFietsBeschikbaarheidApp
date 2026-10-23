package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import dev.jordond.compass.Coordinates
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import nl.ovfietsbeschikbaarheid.ext.isInTheNetherlands
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.ui.theme.Blue90
import nl.ovfietsbeschikbaarheid.ui.theme.OvFietsYellow
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKMapPointForCoordinate
import platform.MapKit.MKMapRect
import platform.MapKit.MKMapRectMake
import platform.MapKit.MKMapSizeWorld
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKUserLocation
import platform.MapKit.MKUserTrackingButton
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIScreen
import platform.UIKit.systemBackgroundColor
import platform.darwin.NSObject
import kotlin.math.pow

private const val LOCATION_ANNOTATION_VIEW_ID = "location"
private const val WORLD_WIDTH_AT_ZOOM_LEVEL_0 = 256.0

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeLocationsMap(
    modifier: Modifier,
    locations: List<LocationOverviewModel>,
    userCoordinates: Coordinates?,
    showsUserLocation: Boolean,
    onLocationClick: (LocationOverviewModel) -> Unit,
) {
    var visibleMapRect by rememberSaveable { mutableStateOf<List<Double>?>(null) }
    val currentOnLocationClick by rememberUpdatedState(onLocationClick)
    val mapViewDelegate = remember {
        LocationsMapViewDelegate(
            centersOnFirstUserLocation = visibleMapRect == null && userCoordinates == null,
            onVisibleMapRectChanged = { visibleMapRect = it },
            onLocationClick = { currentOnLocationClick(it) },
        )
    }

    UIKitView(
        factory = {
            MKMapView().apply {
                delegate = mapViewDelegate
                setShowsUserLocation(showsUserLocation)
                setVisibleMapRect(
                    visibleMapRect?.let { MKMapRectMake(it[0], it[1], it[2], it[3]) }
                        ?: userCoordinates?.let { mapRectAround(it, NEIGHBOURHOOD_ZOOM_LEVEL) }
                        ?: mapRectAround(centerOfTheNetherlands, THE_NETHERLANDS_ZOOM_LEVEL)
                )
                addAnnotations(locations.map { LocationAnnotation(it) })
                if (showsUserLocation) {
                    addUserTrackingButton()
                }
            }
        },
        modifier = modifier,
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun mapRectAround(center: Coordinates, zoomLevel: Float): CValue<MKMapRect> {
    val screenWidth = UIScreen.mainScreen.bounds.useContents { size.width }
    val width = MKMapSizeWorld.width * screenWidth / (WORLD_WIDTH_AT_ZOOM_LEVEL_0 * 2.0.pow(zoomLevel.toDouble()))
    return MKMapPointForCoordinate(CLLocationCoordinate2DMake(center.latitude, center.longitude)).useContents {
        MKMapRectMake(x - width / 2, y - width / 2, width, width)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.addUserTrackingButton() {
    val userTrackingButton = MKUserTrackingButton.userTrackingButtonWithMapView(this).apply {
        translatesAutoresizingMaskIntoConstraints = false
        backgroundColor = UIColor.systemBackgroundColor
        layer.cornerRadius = 8.0
    }
    addSubview(userTrackingButton)
    NSLayoutConstraint.activateConstraints(
        listOf(
            userTrackingButton.trailingAnchor.constraintEqualToAnchor(safeAreaLayoutGuide.trailingAnchor, constant = -16.0),
            userTrackingButton.bottomAnchor.constraintEqualToAnchor(safeAreaLayoutGuide.bottomAnchor, constant = -32.0),
        )
    )
}

@OptIn(ExperimentalForeignApi::class)
private class LocationAnnotation(val location: LocationOverviewModel) : MKPointAnnotation() {
    init {
        setCoordinate(CLLocationCoordinate2DMake(location.latitude, location.longitude))
        setTitle(location.title)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class LocationsMapViewDelegate(
    private var centersOnFirstUserLocation: Boolean,
    private val onVisibleMapRectChanged: (List<Double>) -> Unit,
    private val onLocationClick: (LocationOverviewModel) -> Unit,
) : NSObject(), MKMapViewDelegateProtocol {

    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        onVisibleMapRectChanged(mapView.visibleMapRect.useContents { listOf(origin.x, origin.y, size.width, size.height) })
    }

    override fun mapView(mapView: MKMapView, didUpdateUserLocation: MKUserLocation) {
        if (!centersOnFirstUserLocation) {
            return
        }
        centersOnFirstUserLocation = false
        val userCoordinates = didUpdateUserLocation.coordinate.useContents { Coordinates(latitude, longitude) }
        if (userCoordinates.isInTheNetherlands()) {
            mapView.setVisibleMapRect(mapRectAround(userCoordinates, NEIGHBOURHOOD_ZOOM_LEVEL), animated = true)
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        if (viewForAnnotation !is LocationAnnotation) {
            return null
        }
        val annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(LOCATION_ANNOTATION_VIEW_ID) as MKMarkerAnnotationView?
            ?: MKMarkerAnnotationView(viewForAnnotation, LOCATION_ANNOTATION_VIEW_ID).apply {
                markerTintColor = OvFietsYellow.toUIColor()
                glyphTintColor = Blue90.toUIColor()
                glyphImage = UIImage.systemImageNamed("bicycle")
            }
        annotationView.annotation = viewForAnnotation
        return annotationView
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val annotation = didSelectAnnotationView.annotation as? LocationAnnotation ?: return
        mapView.deselectAnnotation(annotation, animated = false)
        onLocationClick(annotation.location)
    }
}

private fun Color.toUIColor() = UIColor(red = red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = alpha.toDouble())
