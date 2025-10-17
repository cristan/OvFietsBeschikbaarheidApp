package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import nl.ovfietsbeschikbaarheid.model.AddressModel
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.baseline_directions_24
import nl.ovfietsbeschikbaarheid.resources.content_description_navigate
import nl.ovfietsbeschikbaarheid.resources.details_coordinates
import nl.ovfietsbeschikbaarheid.resources.location_title
import nl.ovfietsbeschikbaarheid.resources.map_available
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MapComponent(
    location: AddressModel?,
    latitude: Double,
    longitude: Double,
    directions: String?,
    description: String,
    rentalBikesAvailable: Int?,
    onNavigateClicked: (String) -> Unit
) {
    val coordinates = LatLng(latitude, longitude)
    OvCard {
        Text(
            text = stringResource(Res.string.location_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        val onAddressClick = {
            if (location != null) {
                val address =
                    "${location.street} ${location.houseNumber} ${location.postalCode} ${location.city}"
                onNavigateClicked(address)
            } else {
                onNavigateClicked("${coordinates.latitude}, ${coordinates.longitude}")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddressClick)
        ) {
            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (location != null) {
                        Text("${location.street} ${location.houseNumber}")
                        Text("${location.postalCode} ${location.city}")
                    } else {
                        Text(stringResource(Res.string.details_coordinates, coordinates.latitude, coordinates.longitude))
                    }
                }

                Icon(
                    painter = painterResource(Res.drawable.baseline_directions_24),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = stringResource(Res.string.content_description_navigate),
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
        }

        if (directions != null) {
            Text(
                text = directions,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(coordinates, 15f)
        }
        GoogleMap(
            modifier = Modifier
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp)),
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
}