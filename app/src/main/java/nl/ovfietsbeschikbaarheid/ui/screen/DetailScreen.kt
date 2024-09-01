package nl.ovfietsbeschikbaarheid.ui.screen

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.ext.OnReturnToScreenEffect
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.LocationModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OpenState
import nl.ovfietsbeschikbaarheid.model.OpeningHoursModel
import nl.ovfietsbeschikbaarheid.model.ServiceType
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.ui.components.OvCard
import nl.ovfietsbeschikbaarheid.ui.theme.Green50
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Orange50
import nl.ovfietsbeschikbaarheid.ui.theme.Red50
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.ui.view.FullPageLoader
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder

@Composable
fun DetailScreen(
    locationCode: String,
    viewModel: DetailsViewModel = koinViewModel(),
    onAlternativeClicked: (LocationOverviewModel) -> Unit,
    onBackClicked: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.setLocationCode(locationCode)
    }

    val context = LocalContext.current
    val onLocationClicked: (String) -> Unit = { address ->
        val intent = Intent(Intent.ACTION_VIEW)
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

    // Refresh the screen on multitasking back to it
    OnReturnToScreenEffect(viewModel::onReturnToScreenTriggered)

    val title by viewModel.title
    val details by viewModel.screenState
    DetailsView(
        title,
        details,
        viewModel::onRetryClick,
        viewModel::onPullToRefresh,
        onLocationClicked,
        onAlternativeClicked,
        onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsView(
    title: String,
    details: ScreenState<DetailsModel>,
    onRetry: () -> Unit,
    onPullToRefresh: () -> Unit,
    onLocationClicked: (String) -> Unit,
    onAlternativeClicked: (LocationOverviewModel) -> Unit,
    onBackClicked: () -> Unit
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
                        Text(title)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
                        }
                    },
                )
            },
        ) { innerPadding ->
            when (details) {
                ScreenState.FullPageError -> FullPageError(onRetry)
                ScreenState.Loading -> FullPageLoader()
                is ScreenState.Loaded<DetailsModel> -> {
                    PullToRefreshBox(
                        state = rememberPullToRefreshState(),
                        modifier = Modifier.padding(innerPadding),
                        isRefreshing = details.isRefreshing,
                        onRefresh = onPullToRefresh,
                    ) {
                        ActualDetails(details.data, onLocationClicked, onAlternativeClicked)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActualDetails(
    details: DetailsModel,
    onLocationClicked: (String) -> Unit,
    onAlternativeClicked: (LocationOverviewModel) -> Unit
) {
    Surface(
        Modifier.verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp)) {
            MainInfo(details)

            Location(details, onLocationClicked)

            ExtraInfo(details)

            if (details.openingHours.isNotEmpty()) {
                OpeningHours(details)
            }

            if (details.alternatives.isNotEmpty()) {
                Alternatives(details, onAlternativeClicked)
            }
        }
    }
}

@Composable
private fun MainInfo(details: DetailsModel) {
    OvCard {
        Text("OV-fietsen beschikbaar")
        val rentalBikesAvailable = details.rentalBikesAvailable
        val amount = rentalBikesAvailable?.toString() ?: "Onbekend"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = if (rentalBikesAvailable == null) 0f else rentalBikesAvailable.toFloat() / details.capacity
            val color =
                when (details.openState) {
                    is OpenState.Closed -> Red50
                    is OpenState.Closing -> Orange50
                    else -> ProgressIndicatorDefaults.circularColor
                }
            CircularProgressIndicator(
                progress = { progress }, modifier = Modifier.size(220.dp),
                color = color,
                strokeWidth = 36.dp,
                strokeCap = StrokeCap.Butt,
                gapSize = 0.dp
            )
            Text(
                text = amount,
                fontSize = if (rentalBikesAvailable != null) 60.sp else 24.sp
            )
        }
        details.openState?.let { openState ->
            Row(Modifier.align(Alignment.End)) {
                when (openState) {
                    is OpenState.Closed -> {
                        Text(text = "Gesloten", color = Red50)
                        if (openState.openDay != null) {
                            Text(text = " • Gaat ${openState.openDay} open om ${openState.openTime}")
                        } else {
                            Text(text = " • Gaat open om ${openState.openTime}")
                        }
                    }

                    is OpenState.Closing -> {
                        Text(text = "Sluit snel", color = Orange50)
                        Text(" • Sluit om ${openState.closingTime}")
                    }

                    is OpenState.Open -> {
                        Text("Open tot ${openState.closingTime}")
                    }

                    OpenState.Open247 -> Text(text = "24/7 open")
                }
            }
        }
    }
}

@Composable
private fun Location(details: DetailsModel, onNavigateClicked: (String) -> Unit) {
    OvCard(
        contentPadding = 0.dp
    ) {
        Text(
            text = "Locatie",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )
        val onAddressClick = {
            if (details.location != null) {
                val location = details.location
                val address =
                    "${location.street} ${location.houseNumber} ${location.postalCode} ${location.city}"
                onNavigateClicked(address)
            } else {
                onNavigateClicked("${details.coordinates.latitude}, ${details.coordinates.longitude}")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddressClick)
                .padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val location = details.location

                Column(modifier = Modifier.weight(1f)) {
                    if (location != null) {
                        Text("${location.street} ${location.houseNumber}")
                        Text("${location.postalCode} ${location.city}")
                    } else {
                        Text("Coördinaten: ${details.coordinates.latitude}, ${details.coordinates.longitude}")
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.baseline_directions_24),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Navigeer",
                    modifier = Modifier.size(24.dp)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 16.dp))
        }

        if (details.directions != null) {
            Text(
                text = details.directions,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(details.coordinates, 15f)
        }
        Card(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            GoogleMap(
                modifier = Modifier.height(260.dp),
                cameraPositionState = cameraPositionState,
            ) {
                Marker(
                    //                    icon = Icons.Filled.,
                    state = rememberMarkerState(position = details.coordinates),
                    title = details.description,
                    snippet = "${details.rentalBikesAvailable ?: "??"} beschikbaar"
                )
            }
        }
    }
}

@Composable
private fun ExtraInfo(details: DetailsModel) {
    OvCard {
        details.serviceType?.let {
            Row {
                Icon(
                    painter = painterResource(id = it.icon),
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(it.text)
            }
        }

        val bottomPadding = if (details.about != null) 8.dp else 0.dp
        Row(Modifier.padding(bottom = bottomPadding)) {
            Icon(
                painter = painterResource(id = R.drawable.pedal_bike_24px),
                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Totale capaciteit: ${details.capacity}")
        }

        if (details.about != null) {
            val urlText = "ov-fiets.nl/slot"
            if (details.about.contains(urlText)) {
                val parts = details.about.split(urlText)
                val annotatedString = buildAnnotatedString {
                    append(parts[0])
                    withStyledLink(urlText, "https://$urlText")

                    if (parts.size > 1) {
                        append(parts[1])
                    }
                }
                Text(annotatedString)
            } else {
                Text(details.about)
            }
        }
    }
}

@Composable
private fun OpeningHours(details: DetailsModel) {
    OvCard {
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
private fun Alternatives(
    details: DetailsModel,
    onAlternativeClicked: (LocationOverviewModel) -> Unit
) {
    OvCard {
        Text(
            text = if (details.stationName != null) "Op ${details.stationName}" else "Op deze locatie",
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

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
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
    val details = DetailsModel(
        "Hilversum",
        OpenState.Open247,
        openingHours,
        144,
        200,
        ServiceType.Bemenst,
        about,
        directions,
        locationModel,
        LatLng(52.22626, 5.18076),
        "Amsterdam Zuid",
        listOf(
            TestData.testLocationOverviewModel
        ),
    )
    DetailsView(
        "Hilversum",
        ScreenState.Loaded(details),
        {},
        {},
        {},
        {},
        {}
    )
}