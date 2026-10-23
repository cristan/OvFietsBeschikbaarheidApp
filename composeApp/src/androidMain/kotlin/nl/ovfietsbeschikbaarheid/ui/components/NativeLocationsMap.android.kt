package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import dev.jordond.compass.Coordinates
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
    onLocationClick: (LocationOverviewModel) -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = if (userCoordinates != null) {
            CameraPosition.fromLatLngZoom(LatLng(userCoordinates.latitude, userCoordinates.longitude), NEIGHBOURHOOD_ZOOM_LEVEL)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(centerOfTheNetherlands.latitude, centerOfTheNetherlands.longitude), THE_NETHERLANDS_ZOOM_LEVEL)
        }
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = showsUserLocation),
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
    ) {
        locations.forEach { location ->
            key(location.locationCode) {
                MarkerComposable(
                    state = rememberUpdatedMarkerState(LatLng(location.latitude, location.longitude)),
                    contentDescription = location.title,
                    anchor = Offset(0.5f, 0.5f),
                    onClick = {
                        onLocationClick(location)
                        true
                    },
                ) {
                    MarkerIcon()
                }
            }
        }
    }
}

@Composable
private fun MarkerIcon() {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .background(OvFietsYellow, shape)
            .border(1.dp, Color.White, shape)
            .padding(4.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.pedal_bike_24px),
            contentDescription = null,
            tint = Blue90,
        )
    }
}
