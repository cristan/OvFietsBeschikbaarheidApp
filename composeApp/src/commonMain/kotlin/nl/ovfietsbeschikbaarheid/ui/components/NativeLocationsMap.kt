package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.jordond.compass.Coordinates
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel

val centerOfTheNetherlands = Coordinates(52.2129919, 5.2793703)
const val THE_NETHERLANDS_ZOOM_LEVEL = 7f
const val NEIGHBOURHOOD_ZOOM_LEVEL = 13f

@Composable
expect fun NativeLocationsMap(
    modifier: Modifier = Modifier,
    locations: List<LocationOverviewModel>,
    userCoordinates: Coordinates?,
    showsUserLocation: Boolean,
    onLocationsClick: (List<LocationOverviewModel>) -> Unit,
)
