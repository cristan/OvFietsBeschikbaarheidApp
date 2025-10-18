package nl.ovfietsbeschikbaarheid.ui.screen

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.ext.OnReturnToScreenEffect
import nl.ovfietsbeschikbaarheid.ext.dutchTimeZone
import nl.ovfietsbeschikbaarheid.ext.shimmerShape
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.model.AddressModel
import nl.ovfietsbeschikbaarheid.model.CapacityModel
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.GraphDayModel
import nl.ovfietsbeschikbaarheid.model.OpenState
import nl.ovfietsbeschikbaarheid.model.OpeningHoursModel
import nl.ovfietsbeschikbaarheid.model.ServiceType
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.capacity_graph_title
import nl.ovfietsbeschikbaarheid.resources.content_description_back
import nl.ovfietsbeschikbaarheid.resources.day_1
import nl.ovfietsbeschikbaarheid.resources.day_2
import nl.ovfietsbeschikbaarheid.resources.day_3
import nl.ovfietsbeschikbaarheid.resources.day_4
import nl.ovfietsbeschikbaarheid.resources.day_5
import nl.ovfietsbeschikbaarheid.resources.day_6
import nl.ovfietsbeschikbaarheid.resources.day_7
import nl.ovfietsbeschikbaarheid.resources.details_alternatives_at
import nl.ovfietsbeschikbaarheid.resources.details_alternatives_at_this_location
import nl.ovfietsbeschikbaarheid.resources.details_amount_available
import nl.ovfietsbeschikbaarheid.resources.details_amount_unknown
import nl.ovfietsbeschikbaarheid.resources.details_capacity
import nl.ovfietsbeschikbaarheid.resources.details_no_data_message
import nl.ovfietsbeschikbaarheid.resources.details_no_data_title
import nl.ovfietsbeschikbaarheid.resources.location_title
import nl.ovfietsbeschikbaarheid.resources.open_state_closed
import nl.ovfietsbeschikbaarheid.resources.open_state_closes_at
import nl.ovfietsbeschikbaarheid.resources.open_state_closing_soon
import nl.ovfietsbeschikbaarheid.resources.open_state_open_247
import nl.ovfietsbeschikbaarheid.resources.open_state_opens_later_at
import nl.ovfietsbeschikbaarheid.resources.open_state_opens_today_at
import nl.ovfietsbeschikbaarheid.resources.open_until
import nl.ovfietsbeschikbaarheid.resources.opening_hours_title
import nl.ovfietsbeschikbaarheid.resources.pedal_bike_24px
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.ui.components.CapacityGraph
import nl.ovfietsbeschikbaarheid.ui.components.MapComponent
import nl.ovfietsbeschikbaarheid.ui.components.OvCard
import nl.ovfietsbeschikbaarheid.ui.theme.Grey10
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Orange50
import nl.ovfietsbeschikbaarheid.ui.theme.Red50
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsContent
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.util.Locale
import kotlin.time.ExperimentalTime

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

    val onLocationClicked: (String) -> Unit = onLocationClicked()

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
                                contentDescription = stringResource(Res.string.content_description_back)
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
                            FullPageError(
                                title = stringResource(Res.string.details_no_data_title),
                                message = stringResource(Res.string.details_no_data_message, details.data.locationTitle, details.data.formattedLastFetchedDate),
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
    Column(modifier
        .padding(top = 4.dp, start = 20.dp, end = 20.dp)
        .verticalScroll(rememberScrollState())
    ) {
        OvCard {
            Text(stringResource(Res.string.details_amount_available))
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
                    stringResource(Res.string.open_until, "23:33"),
                    modifier = Modifier.shimmerShape(shimmerInstance)
                )
            }
        }

        OvCard {
            Text(
                text = stringResource(Res.string.capacity_graph_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .height(270.dp)
                    .fillMaxWidth()
                    .shimmerShape(shimmerInstance, shape = RoundedCornerShape(8.dp))
            )
        }

        OvCard {
            Text(
                text = stringResource(Res.string.location_title),
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

            if (details.graphDays.isNotEmpty()) {
                CapacityGraph(details.graphDays)
            }

            details.disruptions?.let {
                Disruptions(it)
            }

            MapComponent(
                details.location,
                details.latitude,
                details.longitude,
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
        Text(stringResource(Res.string.details_amount_available))
        val rentalBikesAvailable = details.rentalBikesAvailable
        val amount = rentalBikesAvailable?.toString() ?: stringResource(Res.string.details_amount_unknown)
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
                trackColor = if (isSystemInDarkTheme()) ProgressIndicatorDefaults.circularDeterminateTrackColor else Grey10,
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
                        Text(text = stringResource(Res.string.open_state_closed), color = Red50)
                        if (openState.openDay != null) {
                            val openDay = stringResource(openState.openDay).lowercase(Locale.UK)
                            Text(text = " " + stringResource(Res.string.open_state_opens_later_at, openDay, openState.openTime))
                        } else {
                            Text(text = " " + stringResource(Res.string.open_state_opens_today_at, openState.openTime))
                        }
                    }

                    is OpenState.Closing -> {
                        Text(text = stringResource(Res.string.open_state_closing_soon), color = Orange50)
                        Text(" " + stringResource(Res.string.open_state_closes_at, openState.closingTime))
                    }

                    is OpenState.Open -> {
                        Text(stringResource(Res.string.open_until, openState.closingTime))
                    }

                    OpenState.Open247 -> Text(stringResource(Res.string.open_state_open_247))
                }
            }
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
private fun ExtraInfo(details: DetailsModel) {
    OvCard {
        details.serviceType?.let {
            Row {
                Icon(
                    painter = painterResource(it.icon),
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
                painter = painterResource(Res.drawable.pedal_bike_24px),
                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(Res.string.details_capacity, details.capacity))
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
            text = stringResource(Res.string.opening_hours_title),
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
            text = if (details.stationName != null) stringResource(Res.string.details_alternatives_at, details.stationName) else stringResource(Res.string.details_alternatives_at_this_location),
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
private fun onLocationClicked(): (String) -> Unit {
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
    return onLocationClicked
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

@OptIn(ExperimentalTime::class)
@Preview(heightDp = 2000)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
@Composable
fun DetailsPreview() {
    val dayNames =
        listOf(Res.string.day_1, Res.string.day_2, Res.string.day_3, Res.string.day_4, Res.string.day_5, Res.string.day_6, Res.string.day_7)
    val openingHours = dayNames.map { day ->
        OpeningHoursModel(day, "04:45", "01:15")
    }
    val about =
        "Je huurt hier een OV-fiets met het nieuwe OV-fietsslot, waarbij je OV-chipkaart de sleutel is. Meer informatie vind je op ov-fiets.nl/slot.\r\n\r\nAan de andere kant van het station vind je de bemenste fietsenstalling. Ook hier kun je een OV-fiets huren."
    val directions = "Volg vanaf het station de borden Mondriaanplein. U vindt de stalling recht voor de uitgang van het station."
    val addressModel = AddressModel(
        city = "Amersfoort",
        street = "Piet Mondriaanplein",
        houseNumber = "1",
        postalCode = "3812 GZ",
    )

    val now = LocalDateTime(2025, 7, 12, 11, 35, 30, 500).toInstant(dutchTimeZone)
    val start = LocalDateTime(2025, 7, 12, 0, 1, 25, 250).toInstant(dutchTimeZone)
    val startPrediction = LocalDateTime(2025, 7, 5, 12, 2, 15, 150).toInstant(dutchTimeZone)

    val history = listOf(
        CapacityModel(20, start),
        CapacityModel(19, start.plus(1, DateTimeUnit.HOUR)),
        CapacityModel(18, start.plus(2, DateTimeUnit.HOUR)),
        CapacityModel(16, start.plus(3, DateTimeUnit.HOUR)),
        CapacityModel(16, start.plus(4, DateTimeUnit.HOUR)),
        CapacityModel(13, start.plus(5, DateTimeUnit.HOUR)),
        CapacityModel(0, start.plus(6, DateTimeUnit.HOUR)),
        CapacityModel(2, start.plus(7, DateTimeUnit.HOUR)),
        CapacityModel(22, start.plus(8, DateTimeUnit.HOUR)),
        CapacityModel(18, start.plus(9, DateTimeUnit.HOUR)),
        CapacityModel(14, start.plus(10, DateTimeUnit.HOUR)),
        CapacityModel(15, start.plus(11, DateTimeUnit.HOUR)),
        CapacityModel(14, now)
    )
    val prediction = listOf(
        CapacityModel(25, startPrediction),
        CapacityModel(27, startPrediction.plus(1, DateTimeUnit.HOUR)),
        CapacityModel(28, startPrediction.plus(2, DateTimeUnit.HOUR)),
        CapacityModel(29, startPrediction.plus(3, DateTimeUnit.HOUR)),
        CapacityModel(30, startPrediction.plus(4, DateTimeUnit.HOUR)),
        CapacityModel(29, startPrediction.plus(5, DateTimeUnit.HOUR)),
        CapacityModel(31, startPrediction.plus(6, DateTimeUnit.HOUR)),
        CapacityModel(32, startPrediction.plus(7, DateTimeUnit.HOUR)),
        CapacityModel(31, startPrediction.plus(8, DateTimeUnit.HOUR)),
        CapacityModel(31, startPrediction.plus(9, DateTimeUnit.HOUR)),
        CapacityModel(32, startPrediction.plus(10, DateTimeUnit.HOUR)),
        CapacityModel(32, startPrediction.plus(11, DateTimeUnit.HOUR)),
    )
    val graphDay = GraphDayModel(
        isToday = true,
        "M",
        "Maandag",
        history,
        prediction,
        "",
    )

    val details = DetailsModel(
        "Amersfoort Mondriaanplein",
        OpenState.Open("01:20"),
        "Dit OV-fiets uitgiftepunt is open van een kwartier voor vertrek van de eerste trein tot een kwartier na aankomst van de laatste trein.",
        openingHours,
        19,
        105,
        ServiceType.Sleutelautomaat,
        about,
        "Helaas kunt u hier op dit moment geen OV-fiets huren. Onze excuses voor dit ongemak. Raadpleeg de NS Reisplanner app of website (ns.nl/ov-fiets) voor de dichtstbijzijnde OV-fiets verhuurlocatie.",
        directions,
        addressModel,
        52.15446,
        5.37339,
        "Amersfoort",
        listOf(
            DetailScreenData(
                title = "Amersfoort Centraal",
                locationCode = "amf001",
                fetchTime = 1729539103
            ),
        ),
        listOf(graphDay)
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