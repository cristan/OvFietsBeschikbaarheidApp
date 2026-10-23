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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import dev.jordond.compass.Coordinates
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import nl.ovfietsbeschikbaarheid.ext.isInTheNetherlands
import nl.ovfietsbeschikbaarheid.mapper.MapMarkerMapper
import nl.ovfietsbeschikbaarheid.mapper.WORLD_WIDTH_DP_AT_ZOOM_LEVEL_0
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.MapMarker
import nl.ovfietsbeschikbaarheid.ui.theme.Blue90
import nl.ovfietsbeschikbaarheid.ui.theme.OvFietsYellow
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKFeatureDisplayPriorityRequired
import platform.MapKit.MKFeatureVisibility
import platform.MapKit.MKMapPointForCoordinate
import platform.MapKit.MKMapRect
import platform.MapKit.MKMapRectMake
import platform.MapKit.MKMapRectUnion
import platform.MapKit.MKMapSizeWorld
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKUserLocation
import platform.MapKit.MKUserTrackingButton
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIImage
import platform.UIKit.systemBackgroundColor
import platform.darwin.NSObject
import kotlin.math.log2
import kotlin.math.pow

private const val MAP_MARKER_ANNOTATION_VIEW_ID = "mapMarker"
private const val ZOOM_IN_PADDING = 64.0

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeLocationsMap(
    modifier: Modifier,
    locations: List<LocationOverviewModel>,
    userCoordinates: Coordinates?,
    showsUserLocation: Boolean,
    onLocationsClick: (List<LocationOverviewModel>) -> Unit,
) {
    var visibleMapRect by rememberSaveable { mutableStateOf<List<Double>?>(null) }
    val currentOnLocationsClick by rememberUpdatedState(onLocationsClick)
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp().value.toDouble() }
    val mapViewDelegate = remember {
        LocationsMapViewDelegate(
            locations = locations,
            windowWidth = windowWidth,
            centersOnFirstUserLocation = visibleMapRect == null && userCoordinates == null,
            onVisibleMapRectChanged = { visibleMapRect = it },
            onLocationsClick = { currentOnLocationsClick(it) },
        )
    }

    UIKitView(
        factory = {
            MKMapView().apply {
                delegate = mapViewDelegate
                setShowsUserLocation(showsUserLocation)
                setVisibleMapRect(
                    visibleMapRect?.let { MKMapRectMake(it[0], it[1], it[2], it[3]) }
                        ?: userCoordinates?.let { mapRectAround(it, NEIGHBOURHOOD_ZOOM_LEVEL, windowWidth) }
                        ?: mapRectAround(centerOfTheNetherlands, THE_NETHERLANDS_ZOOM_LEVEL, windowWidth)
                )
                if (showsUserLocation) {
                    addUserTrackingButton()
                }
            }
        },
        update = { mapView -> mapViewDelegate.showLocations(mapView, locations) },
        modifier = modifier,
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun mapRectAround(center: Coordinates, zoomLevel: Float, viewWidth: Double): CValue<MKMapRect> {
    val width = MKMapSizeWorld.width * viewWidth / (WORLD_WIDTH_DP_AT_ZOOM_LEVEL_0 * 2.0.pow(zoomLevel.toDouble()))
    return MKMapPointForCoordinate(CLLocationCoordinate2DMake(center.latitude, center.longitude)).useContents {
        MKMapRectMake(x - width / 2, y - width / 2, width, width)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.zoomLevel(): Int {
    val visibleWidth = visibleMapRect.useContents { size.width }
    val viewWidth = bounds.useContents { size.width }
    return log2(MKMapSizeWorld.width * viewWidth / (WORLD_WIDTH_DP_AT_ZOOM_LEVEL_0 * visibleWidth)).toInt()
}

@OptIn(ExperimentalForeignApi::class)
private fun List<LocationOverviewModel>.boundingMapRect(): CValue<MKMapRect> = map { location ->
    MKMapPointForCoordinate(CLLocationCoordinate2DMake(location.latitude, location.longitude)).useContents {
        MKMapRectMake(x, y, 0.0, 0.0)
    }
}.reduce { mapRect, otherMapRect -> MKMapRectUnion(mapRect, otherMapRect) }

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
private class MapMarkerAnnotation(val mapMarker: MapMarker) : MKPointAnnotation() {
    init {
        setCoordinate(CLLocationCoordinate2DMake(mapMarker.latitude, mapMarker.longitude))
        setTitle(mapMarker.locations.joinToString { it.title })
    }
}

@OptIn(ExperimentalForeignApi::class)
private class LocationsMapViewDelegate(
    private var locations: List<LocationOverviewModel>,
    private val windowWidth: Double,
    private var centersOnFirstUserLocation: Boolean,
    private val onVisibleMapRectChanged: (List<Double>) -> Unit,
    private val onLocationsClick: (List<LocationOverviewModel>) -> Unit,
) : NSObject(), MKMapViewDelegateProtocol {

    fun showLocations(mapView: MKMapView, locations: List<LocationOverviewModel>) {
        if (locations != this.locations) {
            this.locations = locations
            showMapMarkers(mapView)
        }
    }

    override fun mapView(mapView: MKMapView, regionDidChangeAnimated: Boolean) {
        onVisibleMapRectChanged(mapView.visibleMapRect.useContents { listOf(origin.x, origin.y, size.width, size.height) })
        showMapMarkers(mapView)
    }

    private fun showMapMarkers(mapView: MKMapView) {
        val mapMarkers = MapMarkerMapper.getMapMarkers(locations, mapView.zoomLevel())
        val shownAnnotations = mapView.annotations.filterIsInstance<MapMarkerAnnotation>()
        val shownMapMarkers = shownAnnotations.map { it.mapMarker }.toSet()
        mapView.removeAnnotations(shownAnnotations.filter { it.mapMarker !in mapMarkers })
        mapView.addAnnotations(mapMarkers.filter { it !in shownMapMarkers }.map { MapMarkerAnnotation(it) })
    }

    override fun mapView(mapView: MKMapView, didUpdateUserLocation: MKUserLocation) {
        if (!centersOnFirstUserLocation) {
            return
        }
        centersOnFirstUserLocation = false
        val userCoordinates = didUpdateUserLocation.coordinate.useContents { Coordinates(latitude, longitude) }
        if (userCoordinates.isInTheNetherlands()) {
            mapView.setVisibleMapRect(mapRectAround(userCoordinates, NEIGHBOURHOOD_ZOOM_LEVEL, windowWidth), animated = true)
        }
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        val mapMarker = (viewForAnnotation as? MapMarkerAnnotation)?.mapMarker ?: return null
        val annotationView = mapView.dequeueReusableAnnotationViewWithIdentifier(MAP_MARKER_ANNOTATION_VIEW_ID) as MKMarkerAnnotationView?
            ?: MKMarkerAnnotationView(viewForAnnotation, MAP_MARKER_ANNOTATION_VIEW_ID)
        annotationView.annotation = viewForAnnotation
        annotationView.markerTintColor = OvFietsYellow.toUIColor()
        annotationView.glyphTintColor = Blue90.toUIColor()
        annotationView.displayPriority = MKFeatureDisplayPriorityRequired
        if (mapMarker.locations.size == 1) {
            annotationView.glyphText = null
            annotationView.glyphImage = UIImage.systemImageNamed("bicycle")
            annotationView.titleVisibility = MKFeatureVisibility.MKFeatureVisibilityAdaptive
        } else {
            annotationView.glyphText = mapMarker.locations.size.toString()
            annotationView.glyphImage = null
            annotationView.titleVisibility = MKFeatureVisibility.MKFeatureVisibilityHidden
        }
        return annotationView
    }

    @ObjCSignatureOverride
    override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
        val annotation = didSelectAnnotationView.annotation as? MapMarkerAnnotation ?: return
        mapView.deselectAnnotation(annotation, animated = false)
        val mapMarker = annotation.mapMarker
        if (MapMarkerMapper.separatesWhenZoomingIn(mapMarker)) {
            val padding = UIEdgeInsetsMake(ZOOM_IN_PADDING, ZOOM_IN_PADDING, ZOOM_IN_PADDING, ZOOM_IN_PADDING)
            mapView.setVisibleMapRect(mapMarker.locations.boundingMapRect(), edgePadding = padding, animated = true)
        } else {
            onLocationsClick(mapMarker.locations)
        }
    }
}

private fun Color.toUIColor() = UIColor(red = red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = alpha.toDouble())
