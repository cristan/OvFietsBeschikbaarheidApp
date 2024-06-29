package com.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.ovfietsbeschikbaarheid.DetailsViewModel
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.OpeningHoursModel
import com.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme

@Composable
fun DetailScreen(overviewModel: LocationOverviewModel, onBackClicked: () -> Unit) {
    val viewModel = viewModel<DetailsViewModel>()
    viewModel.setDetailUrl(overviewModel.uri)

    OVFietsBeschikbaarheidTheme {
        val details by viewModel.detailsPayload.collectAsState()
        DetailsView(overviewModel.title, details, onBackClicked)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsView(title: String, details: DetailsModel?, onBackClicked: () -> Unit) {
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
        details?.let {
            Column(
                Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Row {
                    Text("OV-fietsen beschikbaar:")
                }
                val amount = details.rentalBikesAvailable?.toString() ?: "Onbekend"
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = amount,
                        fontSize = 48.sp
                    )
                }
                details.serviceType?.let {
                    Text(it)
                }

                Row {
                    when (details.open) {
                        true -> Text(text = "Open")
                        false -> Text(text = "Gesloten")
                    }
                }
                if (details.openingHours.isNotEmpty()) {
                    Text("Openingstijden", style = MaterialTheme.typography.headlineMedium)
                }
                details.openingHours.forEach {
                    Row(Modifier.fillMaxWidth()) {
                        Text(it.dayOfWeek, Modifier.weight(1f))
                        Text("${it.startTime} - ${it.endTime}", Modifier.weight(2f))
                    }
                }
                if (details.location != null) {
                    Text("Adres", style = MaterialTheme.typography.headlineMedium)
                    Text("${details.location.street} ${details.location.houseNumber}")
                    Text("${details.location.postalCode} ${details.location.city}")
                }
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
                if (details.directions != null) {
                    Text("\n" + details.directions)
                }
                if (details.about != null) {
                    Text("\n" + details.about)
                }
            }
        }
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
            true,
            openingHours,
            144,
            "Bemenst",
            about,
            directions,
            locationModel,
            LatLng(52.22626, 5.18076)
        ), {}
    )
}