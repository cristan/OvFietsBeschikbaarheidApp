package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.ext.OnReturnToScreenEffect
import nl.ovfietsbeschikbaarheid.ext.shimmerShape
import nl.ovfietsbeschikbaarheid.mapper.OpenStateMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.model.LocationType
import nl.ovfietsbeschikbaarheid.model.OpenState
import nl.ovfietsbeschikbaarheid.ui.theme.Gray80
import nl.ovfietsbeschikbaarheid.ui.theme.Indigo05
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Orange50
import nl.ovfietsbeschikbaarheid.ui.theme.Red50
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.viewmodel.AskPermissionState
import nl.ovfietsbeschikbaarheid.viewmodel.HomeContent
import nl.ovfietsbeschikbaarheid.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onInfoClicked: () -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit
) {
    val searchTerm by viewModel.searchTerm
    val screen by viewModel.content

    LaunchedEffect(Unit) {
        viewModel.onScreenLaunched()
    }
    // Check if permissions changed after returning to the screen
    OnReturnToScreenEffect {
        viewModel.onReturnedToScreen()
    }

    HomeView(
        searchTerm,
        screen,
        viewModel::onSearchTermChanged,
        viewModel::onRequestPermissionsClicked,
        viewModel::onTurnOnGpsClicked,
        viewModel::onPullToRefresh,
        onInfoClicked,
        onLocationClick,
        viewModel::onRetryClicked,
    )
}

@Composable
private fun HomeView(
    searchTerm: String,
    screen: HomeContent,
    onSearchTermChanged: (String) -> Unit,
    onRequestLocationClicked: (AskPermissionState) -> Unit,
    onTurnOnGpsClicked: () -> Unit,
    onPullToRefresh: () -> Unit,
    onInfoClicked: () -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit,
    onRetryClick: () -> Unit
) {
    OVFietsBeschikbaarheidTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                HomeTopAppBar(onInfoClicked)
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SearchField(searchTerm, onSearchTermChanged)

                when (screen) {
                    HomeContent.InitialEmpty -> Unit
                    is HomeContent.AskGpsPermission -> {
                        AskForGpsPermission(screen.state, onRequestLocationClicked)
                    }

                    HomeContent.GpsTurnedOff -> {
                        GpsRequestSomething(
                            stringResource(R.string.gps_off_rationale),
                            stringResource(R.string.gps_off_button),
                            false,
                            onTurnOnGpsClicked
                        )
                    }

                    is HomeContent.NoGpsLocation -> {
                        Text(
                            text = stringResource(R.string.home_no_gps_location),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    HomeContent.Loading -> {
                        val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
                        LazyColumn(userScrollEnabled = false) {
                            item {
                                HorizontalBar(stringResource(R.string.home_nearby))
                            }
                            items(count = 20) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row {
                                            Box(Modifier.size(width = 140.dp, height = 24.dp).padding(bottom = 4.dp, top = 2.dp).shimmerShape(shimmerInstance))
                                        }
                                        Row {
                                            Box(Modifier.size(width = 60.dp, height = 20.dp).padding(top = 4.dp).shimmerShape(shimmerInstance))
                                        }
                                    }

                                    Icon(
                                        painter = painterResource(id = R.drawable.pedal_bike_24px),
                                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    Box(
                                        modifier = Modifier.padding(start = 8.dp),
                                    ) {
                                        Box(Modifier.size(width = 24.dp, height = 20.dp).padding(top = 4.dp).shimmerShape(shimmerInstance))

                                        // Placeholder so all numbers are left aligned. Text, so it scales when people have a larger font size
                                        Text(
                                            text = "888",
                                            color = Color.Transparent
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HomeContent.NetworkError -> {
                        FullPageError(onRetry = onRetryClick)
                    }

                    is HomeContent.GpsContent -> {
                        GpsContent(screen, onLocationClick, onPullToRefresh)
                    }

                    is HomeContent.NoSearchResults -> {
                        Text(
                            text = stringResource(R.string.home_no_search_results_for, screen.searchTerm),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    is HomeContent.SearchTermContent -> {
                        // TODO?: add a pull to refresh here as well
                        LazyColumn {
                            items(screen.locations) { location ->
                                LocationCard(location) {
                                    onLocationClick(location)
                                }
                            }
                            screen.nearbyLocations?.let { nearbyLocations ->
                                item {
                                    HorizontalBar(stringResource(R.string.home_search_results_for, screen.searchTerm))
                                }
                                items(nearbyLocations) { location ->
                                    LocationCard(location.location, location.distance) {
                                        onLocationClick(location.location)
                                    }
                                }
                            }
                            item {
                                Spacer(
                                    Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeTopAppBar(onInfoClicked: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            actionIconContentColor = Yellow50,
            titleContentColor = Yellow50,
        ),
        title = {
            Text(stringResource(R.string.app_name))
        },
        actions = {
            IconButton(onClick = onInfoClicked) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GpsContent(
    gpsContent: HomeContent.GpsContent,
    onLocationClick: (LocationOverviewModel) -> Unit,
    onRefresh: () -> Unit
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        state = state,
        isRefreshing = gpsContent.isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn {
            item {
                HorizontalBar(stringResource(R.string.home_nearby))
            }
            items(gpsContent.locations) { location ->
                LocationCard(location.location, location.distance) {
                    onLocationClick(location.location)
                }
            }
            item {
                Spacer(
                    Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)
                )
            }
        }
    }
}

@Composable
private fun HorizontalBar(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSystemInDarkTheme()) Gray80 else Indigo05
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun AskForGpsPermission(state: AskPermissionState, onRequestLocationClicked: (AskPermissionState) -> Unit) {
    val rationaleText = when (state) {
        AskPermissionState.Initial -> null
        AskPermissionState.Denied -> stringResource(R.string.gps_rationale_denied_rationale)
        AskPermissionState.DeniedPermanently -> stringResource(R.string.gps_rationale_denied_permanently_rationale)
    }

    val buttonText = when (state) {
        AskPermissionState.Initial -> stringResource(R.string.gps_rationale_initial_button)
        AskPermissionState.Denied -> stringResource(R.string.gps_rationale_denied_button)
        AskPermissionState.DeniedPermanently -> stringResource(R.string.gps_rationale_denied_permanently_button)
    }

    val showButtonIcon = state == AskPermissionState.Initial
    GpsRequestSomething(rationaleText, buttonText, showButtonIcon) { onRequestLocationClicked(state) }
}

@Composable
private fun GpsRequestSomething(
    rationaleText: String?,
    buttonText: String,
    showButtonIcon: Boolean,
    onDoTheThingClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.map_white_background),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .width(260.dp)
        )

        if (rationaleText != null) {
            Text(
                text = rationaleText,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = onDoTheThingClicked,
            modifier = Modifier.padding(bottom = 64.dp)
        ) {
            if (showButtonIcon) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Text(text = buttonText, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SearchField(
    searchTerm: String,
    onSearchTermChanged: (String) -> Unit
) {
    OutlinedTextField(
        value = searchTerm,
        onValueChange = {
            onSearchTermChanged(it)
        },
        label = { Text(stringResource(R.string.home_search_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        singleLine = true,
        trailingIcon = {
            if (searchTerm.isNotEmpty()) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = stringResource(R.string.content_description_clear_location),
                    modifier = Modifier
                        .clickable {
                            onSearchTermChanged("")
                        }
                )
            }
        }
    )
}

@Composable
fun LocationCard(location: LocationOverviewModel, distance: String? = null, onClick: () -> Unit) {
    val openState = if (location.openingHours == null) null else OpenStateMapper.getOpenState(
        location.locationCode, location.openingHours, LocalDateTime.now(TimeZone.getTimeZone("Europe/Amsterdam").toZoneId())
    )
    val hasDataInBottom = distance != null || openState is OpenState.Closed || openState is OpenState.Closing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = if (hasDataInBottom) 8.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = location.title,
                )
            }
            Row {
                if (distance != null) {
                    Text(
                        text = distance,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                if (location.openingHours != null) {
                    if (openState is OpenState.Closed) {
                        if (distance != null) {
                            Text(
                                text = " • ",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = stringResource(R.string.open_state_closed),
                            color = Red50,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (openState.openDay == null) {
                            Text(
                                text = " " + stringResource(R.string.open_state_opens_today_at, openState.openTime),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    if (openState is OpenState.Closing) {
                        if (distance != null) {
                            Text(
                                text = " • ",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = stringResource(R.string.open_state_closing_soon),
                            color = Orange50,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = " " + stringResource(R.string.open_state_closes_at, openState.closingTime),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        location.rentalBikesAvailable?.let {
            val iconRes = if (location.type == LocationType.EBike) R.drawable.baseline_electric_bike_24 else R.drawable.pedal_bike_24px
            Icon(
                painter = painterResource(id = iconRes),
                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Box(
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(text = it.toString())

                // Placeholder so all numbers are left aligned. Text, so it scales when people have a larger font size
                Text(
                    text = "888",
                    color = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun TestHomeView(searchTerm: String, content: HomeContent) {
    HomeView(searchTerm, content, {}, {}, {}, {}, {}, {}, {})
}

@Preview
@Composable
fun SearchResultsPreview() {
    val locations = listOf(
        TestData.testLocationOverviewModel.copy(
            title = "Amsterdam Zuid Mahlerplein",
            rentalBikesAvailable = 329
        ),
        TestData.testLocationOverviewModel.copy(
            title = "Amsterdam Zuid Zuidplein",
            rentalBikesAvailable = 300
        ),
    )
    TestHomeView("Amsterdam Zuid", HomeContent.SearchTermContent(locations, "Amsterdam Zuid", null))
}

@Preview
@Composable
fun SearchResultsNearbyPreview() {
    val locations = listOf(
        TestData.testLocationOverviewModel.copy(
            title = "Amsterdam Zuid Mahlerplein",
            rentalBikesAvailable = 9
        ),
        TestData.testLocationOverviewModel.copy(
            title = "Amsterdam Zuid Zuidplein",
        ),
    )
    val gpsLocations = listOf(
        LocationOverviewWithDistanceModel(
            "800 m",
            TestData.testLocationOverviewModel.copy(
                title = "Amsterdam Zuid Mahlerplein",
                rentalBikesAvailable = 9
            )
        ),
        LocationOverviewWithDistanceModel(
            "1,1 km",
            TestData.testLocationOverviewModel.copy(
                title = "Amsterdam Zuid Zuidplein",
                rentalBikesAvailable = 101
            )
        ),
    )
    TestHomeView("Amsterdam Zuid", HomeContent.SearchTermContent(locations, "Amsterdam Zuid", gpsLocations))
}

@Preview
@Composable
fun NoResultsPreview() {
    TestHomeView("notFound", HomeContent.NoSearchResults("notFound"))
}

@Preview
@Composable
fun AskForGpsPermissionPreview() {
    TestHomeView("", HomeContent.AskGpsPermission(AskPermissionState.Initial))
}

@Preview
@Composable
fun AskForGpsPermissionWithRationalePreview() {
    TestHomeView("", HomeContent.AskGpsPermission(AskPermissionState.Denied))
}

@Preview
@Composable
fun NoGpsPreview() {
    TestHomeView("", HomeContent.GpsTurnedOff)
}

@Preview
@Composable
fun LoadingPreview() {
    TestHomeView("", HomeContent.Loading)
}

@Preview
@Composable
fun NetworkErrorPreview() {
    TestHomeView("", HomeContent.NetworkError)
}

@Preview
@Composable
fun NoGpsLocationPreview() {
    TestHomeView("", HomeContent.NoGpsLocation)
}

@Preview
@Composable
fun GpsResultsPreview() {
    val locations = listOf(
        LocationOverviewWithDistanceModel(
            "800 m",
            TestData.testLocationOverviewModel.copy(
                title = "Amsterdam Zuid Mahlerplein",
                rentalBikesAvailable = 13
            )
        ),
        LocationOverviewWithDistanceModel(
            "1,1 km",
            TestData.testLocationOverviewModel.copy(
                title = "Amsterdam Zuid Zuidplein",
            )
        ),
    )
    TestHomeView("", HomeContent.GpsContent(locations, Instant.ofEpochMilli(1731442462000)))
}