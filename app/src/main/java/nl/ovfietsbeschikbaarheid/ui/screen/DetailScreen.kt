package nl.ovfietsbeschikbaarheid.ui.screen

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.ext.OnReturnToScreenEffect
import nl.ovfietsbeschikbaarheid.ext.shimmerShape
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.LocationModel
import nl.ovfietsbeschikbaarheid.model.OpenState
import nl.ovfietsbeschikbaarheid.model.OpeningHoursModel
import nl.ovfietsbeschikbaarheid.model.ServiceType
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.ui.components.OvCard
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Orange50
import nl.ovfietsbeschikbaarheid.ui.theme.Red50
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsContent
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun DetailScreen(
    detailScreenData: DetailScreenData,
    viewModel: DetailsViewModel = koinViewModel(),
    onAlternativeClicked: (DetailScreenData) -> Unit,
    onBackClicked: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.screenLaunched(detailScreenData)
    }

    val context = LocalContext.current
    val onLocationClicked: (String) -> Unit = { address ->
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = "http://maps.google.co.in/maps?q=${
            URLEncoder.encode(
                address,
                "UTF-8"
            )
        }".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    // Refresh the screen on multitasking back to it
    OnReturnToScreenEffect(viewModel::onReturnToScreenTriggered)

    val details by viewModel.screenState
    DetailsView(
        detailScreenData,
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
    detailScreenData: DetailScreenData,
    details: ScreenState<DetailsContent>,
    onRetry: () -> Unit,
    onPullToRefresh: () -> Unit,
    onLocationClicked: (String) -> Unit,
    onAlternativeClicked: (DetailScreenData) -> Unit,
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
                        Text(detailScreenData.title)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back)
                            )
                        }
                    },
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            when (details) {
                ScreenState.FullPageError -> FullPageError(onRetry = onRetry)
                ScreenState.Loading -> DetailsLoader(modifier = Modifier.padding(innerPadding))
                is ScreenState.Loaded<DetailsContent> -> {
                    when (details.data) {
                        is DetailsContent.Content -> PullToRefreshBox(
                            state = rememberPullToRefreshState(),
                            modifier = Modifier.padding(innerPadding),
                            isRefreshing = details.isRefreshing,
                            onRefresh = onPullToRefresh,
                        ) {
                            ActualDetails(details.data.details, onLocationClicked, onAlternativeClicked)
                        }

                        is DetailsContent.NotFound -> {
                            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("nl"))
                            val formattedDate = formatter.format(details.data.lastFetched)
                            FullPageError(
                                title = stringResource(R.string.details_no_data_title),
                                message = stringResource(R.string.details_no_data_message, details.data.locationTitle, formattedDate),
                                onRetry = onRetry
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsLoader(
    modifier: Modifier
) {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    Column(modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp)) {
        OvCard {
            Text(stringResource(R.string.details_amount_available))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1.0f },
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(220.dp)
                        .shimmer(shimmerInstance),
                    strokeWidth = 36.dp,
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,

                    )
                Box(
                    modifier = Modifier
                        .size(width = 68.dp, height = 54.dp)
                        .shimmerShape(shimmerInstance)
                )
            }
            Row(Modifier.align(Alignment.End)) {
                Text(
                    stringResource(R.string.open_until, "23:33"),
                    modifier = Modifier.shimmerShape(shimmerInstance)
                )
            }
        }

        OvCard {
            Text(
                text = "Locatie",
                style = MaterialTheme.typography.headlineMedium,
            )

            Box(
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .height(28.dp)
                    .fillMaxWidth()
                    .shimmerShape(shimmerInstance, shape = RoundedCornerShape(8.dp))
            )

            Box(
                modifier = Modifier
                    .height(276.dp)// 260 dp + 16 dp padding. Not sure where that 16 dp comes from, but ok.
                    .fillMaxWidth()
                    .shimmerShape(shimmerInstance, shape = RoundedCornerShape(12.dp))
            )
        }

        OvCard {
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .shimmerShape(shimmerInstance, shape = RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
private fun ActualDetails(
    details: DetailsModel,
    onLocationClicked: (String) -> Unit,
    onAlternativeClicked: (DetailScreenData) -> Unit
) {
    Surface(
        Modifier.verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp)) {
            MainInfo(details)

            CapacityGraph(details.capacityHistory)

            details.disruptions?.let {
                Disruptions(it)
            }

            Location(
                details.location,
                details.coordinates,
                details.directions,
                details.description,
                details.rentalBikesAvailable,
                onLocationClicked
            )

            ExtraInfo(details)

            if (details.openingHours.isNotEmpty()) {
                OpeningHours(details)
            }

            if (details.alternatives.isNotEmpty()) {
                Alternatives(details, onAlternativeClicked)
            }
            Spacer(
                Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)
            )
        }
    }
}

@Composable
private fun MainInfo(details: DetailsModel, lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
    OvCard {
        Text(stringResource(R.string.details_amount_available))
        val rentalBikesAvailable = details.rentalBikesAvailable
        val amount = rentalBikesAvailable?.toString() ?: "Onbekend"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val color =
                when (details.openState) {
                    is OpenState.Closed -> Red50
                    is OpenState.Closing -> Orange50
                    else -> ProgressIndicatorDefaults.circularColor
                }

            var progress by remember { mutableFloatStateOf(0F) }
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = SpringSpec(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            LaunchedEffect(lifecycleOwner) {
                progress = if (rentalBikesAvailable == null) 0f else rentalBikesAvailable.toFloat() / details.capacity
            }

            CircularProgressIndicator(
                progress = { animatedProgress }, modifier = Modifier.size(220.dp),
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
                        Text(text = stringResource(R.string.open_state_closed), color = Red50)
                        if (openState.openDay != null) {
                            val openDay = stringResource(openState.openDay).lowercase(Locale.UK)
                            Text(text = " " + stringResource(R.string.open_state_opens_later_at, openDay, openState.openTime))
                        } else {
                            Text(text = " " + stringResource(R.string.open_state_opens_today_at, openState.openTime))
                        }
                    }

                    is OpenState.Closing -> {
                        Text(text = stringResource(R.string.open_state_closing_soon), color = Orange50)
                        Text(" " + stringResource(R.string.open_state_closes_at, openState.closingTime))
                    }

                    is OpenState.Open -> {
                        Text(stringResource(R.string.open_until, openState.closingTime))
                    }

                    OpenState.Open247 -> Text(stringResource(R.string.open_state_open_247))
                }
            }
        }
    }
}

@Composable
fun CapacityGraph(
    data: List<CapacityModel>,
    modifier: Modifier = Modifier
) {
    val now = remember { Instant.now() }
    val startTime = remember { now.minus(12, ChronoUnit.HOURS) }

    val filtered = remember(data) {
        data.filter { it.dateTime >= startTime }
            .sortedBy { it.dateTime }
    }

    if (filtered.size < 2) return // not enough data

    OvCard {
        // Y axis always starts at 0
        val maxCapacity = filtered.maxOf { it.capacity }.coerceAtLeast(1)

        val primaryColor = MaterialTheme.colorScheme.primary
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val width = size.width
            val height = size.height

            val duration = Duration.between(startTime, now).toMillis().toFloat()

            val points = filtered.map { model ->
                val x = (Duration.between(startTime, model.dateTime).toMillis() / duration) * width
                val y = height - (model.capacity / maxCapacity.toFloat()) * height
                Offset(x, y)
            }

            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (pt in points.drop(1)) {
                    lineTo(pt.x, pt.y)
                }
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun Disruptions(disruptions: String) {
    Card(
        modifier = Modifier.padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Red50,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(disruptions, color = Color.White)
        }
    }
}

@Composable
private fun Location(
    location: LocationModel?,
    coordinates: LatLng,
    directions: String?,
    description: String,
    rentalBikesAvailable: Int?,
    onNavigateClicked: (String) -> Unit
) {
    OvCard {
        Text(
            text = "Locatie",
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
                        Text(stringResource(R.string.details_coordinates, coordinates.latitude, coordinates.longitude))
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.baseline_directions_24),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = stringResource(R.string.content_description_navigate),
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
                snippet = stringResource(R.string.map_available, rentalBikesAvailable?.toString() ?: "??")
            )
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
                Text(stringResource(it.textRes))
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
            Text(stringResource(R.string.details_capacity, details.capacity))
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
            text = stringResource(R.string.opening_hours_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        details.openingHoursInfo?.let {
            Text(it, Modifier.padding(bottom = 8.dp))
        }
        details.openingHours.forEach {
            Row(Modifier.fillMaxWidth()) {
                Text(stringResource(it.dayOfWeek), Modifier.weight(1f))
                Text("${it.startTime} - ${it.endTime}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Alternatives(
    details: DetailsModel,
    onAlternativeClicked: (DetailScreenData) -> Unit
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

@Preview(heightDp = 2000)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
@Composable
fun DetailsLoadingPreview() {
    DetailsView(
        TestData.testDetailScreenData,
        ScreenState.Loading,
        {},
        {},
        {},
        {},
        {}
    )
}

@Preview(heightDp = 2000)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
@Composable
fun DetailsPreview() {
    val dayNames =
        listOf(R.string.day_1, R.string.day_2, R.string.day_3, R.string.day_4, R.string.day_5, R.string.day_6, R.string.day_7)
    val openingHours = dayNames.map { day ->
        OpeningHoursModel(day, "04:45", "01:15")
    }
    val about =
        "Je huurt hier een OV-fiets met het nieuwe OV-fietsslot, waarbij je OV-chipkaart de sleutel is. Meer informatie vind je op ov-fiets.nl/slot.\r\n\r\nAan de andere kant van het station vind je de bemenste fietsenstalling. Ook hier kun je een OV-fiets huren."
    val directions = "Volg vanaf het station de borden Mondriaanplein. U vindt de stalling recht voor de uitgang van het station."
    val locationModel = LocationModel(
        city = "Amersfoort",
        street = "Piet Mondriaanplein",
        houseNumber = "1",
        postalCode = "3812 GZ",
    )
    val details = DetailsModel(
        "Amersfoort Mondriaanplein",
        OpenState.Open("01:20"),
        "Dit OV-fiets uitgiftepunt is open van een kwartier voor vertrek van de eerste trein tot een kwartier na aankomst van de laatste trein.",
        openingHours,
        68,
        105,
        ServiceType.Sleutelautomaat,
        about,
        "Helaas kunt u hier op dit moment geen OV-fiets huren. Onze excuses voor dit ongemak. Raadpleeg de NS Reisplanner app of website (ns.nl/ov-fiets) voor de dichtstbijzijnde OV-fiets verhuurlocatie.",
        directions,
        locationModel,
        LatLng(52.15446, 5.37339),
        "Amersfoort",
        listOf(
            DetailScreenData(
                title = "Amersfoort Centraal",
                uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-amf001",
                locatonCode = "amf001",
                fetchTime = 1729539103
            ),
        ),
        listOf(
            CapacityModel(20, Instant.now().minus(12, ChronoUnit.HOURS)),
            CapacityModel(19, Instant.now().minus(11, ChronoUnit.HOURS)),
            CapacityModel(18, Instant.now().minus(10, ChronoUnit.HOURS)),
            CapacityModel(16, Instant.now().minus(9, ChronoUnit.HOURS)),
            CapacityModel(16, Instant.now().minus(8, ChronoUnit.HOURS)),
            CapacityModel(13, Instant.now().minus(7, ChronoUnit.HOURS)),
            CapacityModel(14, Instant.now().minus(6, ChronoUnit.HOURS)),
            CapacityModel(15, Instant.now().minus(5, ChronoUnit.HOURS)),
            CapacityModel(22, Instant.now().minus(4, ChronoUnit.HOURS)),
            CapacityModel(18, Instant.now().minus(3, ChronoUnit.HOURS)),
            CapacityModel(14, Instant.now().minus(2, ChronoUnit.HOURS)),
            CapacityModel(15, Instant.now().minus(1, ChronoUnit.HOURS)),
            CapacityModel(19, Instant.now())
        ),
    )
    DetailsView(
        TestData.testDetailScreenData,
        ScreenState.Loaded(DetailsContent.Content(details)),
        {},
        {},
        {},
        {},
        {}
    )
}