package com.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ovfietsbeschikbaarheid.R
import com.ovfietsbeschikbaarheid.TestData
import com.ovfietsbeschikbaarheid.ext.OnReturnToScreenEffect
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import com.ovfietsbeschikbaarheid.ui.theme.Gray80
import com.ovfietsbeschikbaarheid.ui.theme.Indigo05
import com.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import com.ovfietsbeschikbaarheid.ui.theme.Yellow50
import com.ovfietsbeschikbaarheid.viewmodel.HomeContent
import com.ovfietsbeschikbaarheid.viewmodel.LocationsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: LocationsViewModel = koinViewModel(),
    onInfoClicked: () -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit
) {
    val searchTerm by viewModel.searchTerm
    val screen by viewModel.content

    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }
    // Check if permissions changed after returning to the screen
    OnReturnToScreenEffect {
        viewModel.checkPermission()
    }

    HomeView(
        searchTerm,
        screen,
        viewModel::onSearchTermChanged,
        viewModel::fetchLocation,
        onInfoClicked,
        onLocationClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeView(
    searchTerm: String,
    screen: HomeContent,
    onSearchTermChanged: (String) -> Unit,
    onRequestLocationClicked: () -> Unit,
    onInfoClicked: () -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit
) {
    OVFietsBeschikbaarheidTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
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
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SearchField(searchTerm, onSearchTermChanged)

                when (screen) {
                    HomeContent.InitialEmpty -> Unit
                    HomeContent.AskForGpsPermission -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
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
                            Button(onClick = onRequestLocationClicked,
                                modifier = Modifier.padding(bottom = 64.dp)) {
                                Icon(Icons.Outlined.Place, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                                Text(text = "OV Fiets locaties in je buurt", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    is HomeContent.GpsError -> {
                        Text(
                            text = screen.message,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    HomeContent.LoadingGpsLocation -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(64.dp).padding(16.dp),
                            )
                            Text("GPS aan het laden.")
                        }
                    }

                    is HomeContent.GpsContent -> {
                        LazyColumn {
                            item {
                                Text(
                                    text = "In de buurt",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSystemInDarkTheme()) Gray80 else Indigo05
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(screen.locations) { location ->
                                LocationCard(location.location, location.distance) {
                                    onLocationClick(location.location)
                                }
                            }
                        }
                    }

                    is HomeContent.NoSearchResults -> {
                        Text(
                            text = "Geen zoekresultaten voor \"${screen.searchTerm}\"",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    is HomeContent.SearchTermContent -> {
                        LazyColumn {
                            items(screen.locations) { location ->
                                LocationCard(location) {
                                    onLocationClick(location)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    searchTerm: String,
    onSearchTermChanged: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = searchTerm,
        onValueChange = {
            onSearchTermChanged(it)
        },
        label = { Text("Zoekterm") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        singleLine = true,
        trailingIcon = {
            if (searchTerm.isNotEmpty()) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier
                        .clickable {
                            keyboardController?.hide()
                            onSearchTermChanged("")
                        }
                )
            }
        }
    )
}

@Composable
fun LocationCard(location: LocationOverviewModel, distance: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Bike icon at the start
        Icon(
            painter = painterResource(id = R.drawable.pedal_bike_24px),
            tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )

        // Location title with weight to take up available space
        Text(
            text = location.title,
            modifier = Modifier.weight(1f)
        )

        // Row to align location icon and distance to the end
        if (distance != null) {
            Text(
                modifier = Modifier
                    .wrapContentWidth(Alignment.End)
                    .padding(start = 8.dp),
                text = distance
            )
        }
    }
}

@Composable
fun TestHomeView(searchTerm: String, content: HomeContent) {
    HomeView(searchTerm, content, {}, {}, {}, {})
}

@Preview
@Composable
fun SearchResultsPreview() {
    val locations = listOf(
        TestData.testLocationOverviewModel.copy(
            title = "Amsterdam Zuid Mahlerplein",
            rentalBikesAvailable = 49
        ),
        TestData.testLocationOverviewModel.copy(
            title = "Amsterdam Zuid Zuidplein",
            rentalBikesAvailable = 148
        ),
    )
    TestHomeView("Amsterdam Zuid", HomeContent.SearchTermContent(locations))
}

@Preview
@Composable
fun NoResultsPreview() {
    TestHomeView("notFound", HomeContent.NoSearchResults("notFound"))
}

@Preview
@Composable
fun AskForGpsPermissionPreview() {
    TestHomeView("", HomeContent.AskForGpsPermission)
}

@Preview
@Composable
fun LoadingGpsPreview() {
    TestHomeView("", HomeContent.LoadingGpsLocation)
}

@Preview
@Composable
fun GpsErrorPreview() {
    TestHomeView("", HomeContent.GpsError("Geef de app toegang tot je locatie om OV fietsen in je buurt te zien."))
}

@Preview
@Composable
fun GpsResultsPreview() {
    val locations = listOf(
        LocationOverviewWithDistanceModel(
            "800 m",
            TestData.testLocationOverviewModel.copy(
                title = "Amsterdam Zuid Mahlerplein",
                rentalBikesAvailable = 49
            )
        ),
        LocationOverviewWithDistanceModel(
            "1,1 km",
            TestData.testLocationOverviewModel.copy(
                title = "Amsterdam Zuid Zuidplein",
                rentalBikesAvailable = 148
            )
        ),
    )
    TestHomeView("", HomeContent.GpsContent(locations))
}