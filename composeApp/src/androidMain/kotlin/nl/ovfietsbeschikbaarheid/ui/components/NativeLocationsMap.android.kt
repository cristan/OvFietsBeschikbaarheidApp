package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import dev.jordond.compass.Coordinates
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.mapper.MapMarkerMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.pedal_bike_24px
import nl.ovfietsbeschikbaarheid.ui.theme.Blue90
import nl.ovfietsbeschikbaarheid.ui.theme.OvFietsYellow
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun NativeLocationsMap(
    modifier: Modifier,
    locations: List<LocationOverviewModel>,
    userCoordinates: Coordinates?,
    showsUserLocation: Boolean,
    onLocationsClick: (List<LocationOverviewModel>) -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = if (userCoordinates != null) {
            CameraPosition.fromLatLngZoom(LatLng(userCoordinates.latitude, userCoordinates.longitude), NEIGHBOURHOOD_ZOOM_LEVEL)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(centerOfTheNetherlands.latitude, centerOfTheNetherlands.longitude), THE_NETHERLANDS_ZOOM_LEVEL)
        }
    }
    val zoomLevel by remember { derivedStateOf { cameraPositionState.position.zoom.toInt() } }
    val mapMarkers = remember(locations, zoomLevel) { MapMarkerMapper.getMapMarkers(locations, zoomLevel) }
    val coroutineScope = rememberCoroutineScope()
    val zoomInPadding = with(LocalDensity.current) { 64.dp.roundToPx() }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = showsUserLocation),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
    ) {
        mapMarkers.forEach { mapMarker ->
            key(mapMarker) {
                MarkerComposable(
                    mapMarker.locations.size,
                    state = rememberUpdatedMarkerState(LatLng(mapMarker.latitude, mapMarker.longitude)),
                    contentDescription = mapMarker.locations.joinToString { it.title },
                    anchor = Offset(0.5f, 0.5f),
                    onClick = {
                        if (MapMarkerMapper.separatesWhenZoomingIn(mapMarker)) {
                            val bounds = LatLngBounds.builder().apply {
                                mapMarker.locations.forEach { include(LatLng(it.latitude, it.longitude)) }
                            }.build()
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, zoomInPadding))
                            }
                        } else {
                            onLocationsClick(mapMarker.locations)
                        }
                        true
                    },
                ) {
                    MarkerIcon(mapMarker.locations.size)
                }
            }
        }
    }
}

@Composable
private fun MarkerIcon(locationCount: Int) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .background(OvFietsYellow, shape)
            .border(1.dp, Color.White, shape)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.pedal_bike_24px),
            contentDescription = null,
            tint = Blue90,
        )
        if (locationCount > 1) {
            Text(
                text = locationCount.toString(),
                color = Blue90,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
