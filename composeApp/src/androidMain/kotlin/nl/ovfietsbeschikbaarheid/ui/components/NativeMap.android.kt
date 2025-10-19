package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.map_available
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun NativeMap(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    description: String,
    rentalBikesAvailable: Int?
) {
    val coordinates = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(coordinates, 15f)
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = { GoogleMapOptions().mapColorScheme(MapColorScheme.FOLLOW_SYSTEM) }
    ) {
        Marker(
            //                    icon = Icons.Filled.,
            state = rememberUpdatedMarkerState(position = coordinates),
            title = description,
            snippet = stringResource(Res.string.map_available, rentalBikesAvailable?.toString() ?: "??")
        )
    }
}