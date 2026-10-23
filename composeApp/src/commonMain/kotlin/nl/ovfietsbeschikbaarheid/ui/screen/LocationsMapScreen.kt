@file:OptIn(ExperimentalTime::class)

package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.toLocalDateTime
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.ext.OnReturnToScreenEffect
import nl.ovfietsbeschikbaarheid.ext.dutchTimeZone
import nl.ovfietsbeschikbaarheid.mapper.OpenStateMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationType
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.baseline_electric_bike_24
import nl.ovfietsbeschikbaarheid.resources.content_description_back
import nl.ovfietsbeschikbaarheid.resources.details_amount_unknown
import nl.ovfietsbeschikbaarheid.resources.map_available
import nl.ovfietsbeschikbaarheid.resources.map_location_details
import nl.ovfietsbeschikbaarheid.resources.map_title
import nl.ovfietsbeschikbaarheid.resources.pedal_bike_24px
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.ui.components.NativeLocationsMap
import nl.ovfietsbeschikbaarheid.ui.components.OpenStateText
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.viewmodel.LocationsMapContent
import nl.ovfietsbeschikbaarheid.viewmodel.LocationsMapViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun LocationsMapScreen(
    viewModel: LocationsMapViewModel = koinViewModel(),
    onLocationClick: (LocationOverviewModel) -> Unit,
    onBackClicked: () -> Unit,
) {
    OnReturnToScreenEffect(viewModel::onReturnedToScreen)

    val screenState by viewModel.screenState
    LocationsMapView(screenState, viewModel::onRetryClick, onLocationClick, onBackClicked)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsMapView(
    screenState: ScreenState<LocationsMapContent>,
    onRetry: () -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit,
    onBackClicked: () -> Unit,
) {
    OVFietsBeschikbaarheidTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = Yellow50,
                        navigationIconContentColor = Yellow50
                    ),
                    title = {
                        Text(stringResource(Res.string.map_title))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.content_description_back)
                            )
                        }
                    },
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            when (screenState) {
                ScreenState.FullPageError -> FullPageError(modifier = Modifier.padding(innerPadding), onClick = onRetry)
                ScreenState.Loading -> Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is ScreenState.Loaded -> LocationsMap(screenState.data, Modifier.padding(innerPadding), onLocationClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsMap(
    content: LocationsMapContent,
    modifier: Modifier,
    onLocationClick: (LocationOverviewModel) -> Unit,
) {
    var selectedLocationCodes by remember { mutableStateOf<List<String>?>(null) }

    NativeLocationsMap(
        modifier = modifier.fillMaxSize(),
        locations = content.locations,
        userCoordinates = content.userCoordinates,
        showsUserLocation = content.showsUserLocation,
        onLocationsClick = { locations -> selectedLocationCodes = locations.map { it.locationCode } },
    )

    selectedLocationCodes?.let { locationCodes ->
        ModalBottomSheet(onDismissRequest = { selectedLocationCodes = null }) {
            content.locations.filter { it.locationCode in locationCodes }.forEachIndexed { index, location ->
                if (index > 0) {
                    HorizontalDivider(Modifier.padding(bottom = 16.dp))
                }
                LocationSummary(location) {
                    selectedLocationCodes = null
                    onLocationClick(location)
                }
            }
        }
    }
}

@Composable
private fun LocationSummary(location: LocationOverviewModel, onDetailsClick: () -> Unit) {
    val openState = location.openingHours?.let {
        OpenStateMapper.getOpenState(location.locationCode, it, Clock.System.now().toLocalDateTime(dutchTimeZone))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = location.title,
            style = MaterialTheme.typography.titleLarge,
        )
        openState?.let {
            OpenStateText(it, Modifier.padding(top = 4.dp))
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconRes = if (location.type == LocationType.EBike) Res.drawable.baseline_electric_bike_24 else Res.drawable.pedal_bike_24px
            Icon(
                painter = painterResource(iconRes),
                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = location.rentalBikesAvailable?.let { stringResource(Res.string.map_available, it.toString()) }
                    ?: stringResource(Res.string.details_amount_unknown),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Button(
            onClick = onDetailsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(stringResource(Res.string.map_location_details))
        }
    }
}

@Preview
@Composable
fun LocationSummaryPreview() {
    OVFietsBeschikbaarheidTheme {
        LocationSummary(TestData.testLocationOverviewModel) {}
    }
}
