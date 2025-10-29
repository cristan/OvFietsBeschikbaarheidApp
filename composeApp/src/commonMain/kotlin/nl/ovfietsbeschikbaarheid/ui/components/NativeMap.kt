package nl.ovfietsbeschikbaarheid.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeMap(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    description: String,
    rentalBikesAvailable: Int?
)