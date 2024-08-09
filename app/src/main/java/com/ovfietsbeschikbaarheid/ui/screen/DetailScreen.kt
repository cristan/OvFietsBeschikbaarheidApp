package com.ovfietsbeschikbaarheid.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.ovfietsbeschikbaarheid.R
import com.ovfietsbeschikbaarheid.TestData
import com.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.OpeningHoursModel
import com.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import java.net.URLEncoder
import java.util.Locale

@Composable
fun DetailScreen(
    locationCode: String,
    onAlternativeClicked: (LocationOverviewModel) -> Unit,
    onBackClicked: () -> Unit
) {
    val viewModel = viewModel<DetailsViewModel>()
    viewModel.setLocationCode(locationCode)

    val context = LocalContext.current
    val onLocationClicked: (LocationModel) -> Unit = { locationModel ->
        val intent = Intent(Intent.ACTION_VIEW)
        val address =
            "${locationModel.street} ${locationModel.houseNumber} ${locationModel.postalCode} ${locationModel.city}"
        intent.data = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=${
                URLEncoder.encode(
                    address,
                    "UTF-8"
                )
            }"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    OVFietsBeschikbaarheidTheme {
        val title by viewModel.title.collectAsState()
        val details by viewModel.detailsPayload.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        DetailsView(title, details, isRefreshing, viewModel::refresh, onLocationClicked, onAlternativeClicked, onBackClicked)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsView(
    title: String,
    details: DetailsModel?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLocationClicked: (LocationModel) -> Unit,
    onAlternativeClicked: (LocationOverviewModel) -> Unit,
    onBackClicked: () -> Unit
) {
    val state = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            state = state,
            modifier = Modifier.padding(innerPadding),
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
        ) {
            ActualDetails(details, onLocationClicked, onAlternativeClicked)
        }
    }
}

@Composable
private fun ActualDetails(
    details: DetailsModel?,
    onLocationClicked: (LocationModel) -> Unit,
    onAlternativeClicked: (LocationOverviewModel) -> Unit
) {
    Surface(
        Modifier
            .verticalScroll(rememberScrollState()),
        //color = if (isSystemInDarkTheme()) Color.DarkGray else Color(0xFFF0F0F0)
    ) {
        details?.let {
            Column(Modifier.padding(20.dp)) {
                DetailsCard {
                    Row {
                        Text("Aantal beschikbaar")
                    }
                    val amount = details.rentalBikesAvailable?.toString() ?: "Onbekend"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = amount,
                            fontSize = if (details.rentalBikesAvailable != null) 88.sp else 40.sp
                        )
                    }
                }

                if (details.location != null) {
                    Address(details.location, onLocationClicked)
                }

                Card(Modifier.padding(top = 8.dp)) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(details.coordinates, 16f)
                    }
                    GoogleMap(
                        modifier = Modifier.height(280.dp),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            //                    icon = Icons.Filled.,
                            state = MarkerState(position = details.coordinates),
                            title = details.description,
                            snippet = "${details.rentalBikesAvailable ?: "??"} beschikbaar"
                        )
                    }
                }

                if (details.serviceType != null || details.directions != null || details.about != null) {
                    Card(
                        Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSystemInDarkTheme()) Color.Unspecified else Color.White,
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            details.serviceType?.let {
                                Text("Type: ${it.lowercase(Locale.UK)}")
                            }
                            if (details.directions != null) {
                                Text("\n" + details.directions)
                            }
                            if (details.about != null) {
                                Text("\n" + details.about)
                            }
                        }
                    }
                }

                if (details.openingHours.isNotEmpty()) {
                    OpeningHours(details)
                }

                if (details.alternatives.isNotEmpty()) {
                    Alternatives(details, onAlternativeClicked)
                }
            }
        }
    }
}

@Composable
private fun Alternatives(
    details: DetailsModel,
    onAlternativeClicked: (LocationOverviewModel) -> Unit
) {
    DetailsCard {
        Text(
            text = "Bij ditzelfde station",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        details.alternatives.forEach {
            TextButton(
                onClick = { onAlternativeClicked(it) }
            ) {
                Text(it.title)
            }
        }
    }
}

@Composable
private fun Address(location: LocationModel, onNavigateClicked: (LocationModel) -> Unit) {
    DetailsCard(
        modifier = Modifier
            .clickable { onNavigateClicked(location) }
    ) {
        Text(
            text = "Adres",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${location.street} ${location.houseNumber}")
                Text("${location.postalCode} ${location.city}")
            }
            Icon(
                painter = painterResource(id = R.drawable.directions_24px),
                contentDescription = "Navigeer"
            )
        }
    }
}

@Composable
private fun OpeningHours(details: DetailsModel) {
    DetailsCard {
        Text(
            text = "Openingstijden",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        details.openingHours.forEach {
            Row(Modifier.fillMaxWidth()) {
                Text(it.dayOfWeek, Modifier.weight(1f))
                Text("${it.startTime} - ${it.endTime}", Modifier.weight(1f))
            }
        }
    }
}


@Composable
fun DetailsCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) Color.Unspecified else Color.White,
        ),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Preview
@Composable
fun DetailsPreview() {
    val dayNames =
        listOf("Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag", "Zondag")
    val openingHours = dayNames.map { day ->
        OpeningHoursModel(day, "04:45", "01:00")
    }
    val about =
        "Je huurt hier zelf een OV-fiets met het nieuwe OV-fietsslot, waarbij je OV-chipkaart de sleutel is. Meer informatie vind je op ov-fiets.nl/slot."
    val directions = "U vindt de stationsstalling nabij spoor 1, de ingang is in de tunnel."
    val locationModel = LocationModel(
        city = "Hilversum",
        street = "Stationsplein",
        houseNumber = "1",
        postalCode = "1211 EX",
    )
    DetailsView(
        "Hilversum",
        DetailsModel(
            "Hilversum",
            openingHours,
            144,
            "Bemenst",
            about,
            directions,
            locationModel,
            LatLng(52.22626, 5.18076),
            listOf(
                TestData.testLocationOverviewModel
            )
        ),
        false,
        {},
        {},
        {},
        {}
    )
}