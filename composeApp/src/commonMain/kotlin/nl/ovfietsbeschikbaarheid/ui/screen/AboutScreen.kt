package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_app_text_1
import nl.ovfietsbeschikbaarheid.resources.about_app_text_1_on_github
import nl.ovfietsbeschikbaarheid.resources.about_app_text_2
import nl.ovfietsbeschikbaarheid.resources.about_app_text_3
import nl.ovfietsbeschikbaarheid.resources.about_app_text_4
import nl.ovfietsbeschikbaarheid.resources.about_app_title
import nl.ovfietsbeschikbaarheid.resources.about_lock_text_1
import nl.ovfietsbeschikbaarheid.resources.about_lock_text_2
import nl.ovfietsbeschikbaarheid.resources.about_lock_title
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_subtitle_with_amount
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_subtitle_without_amount
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_text_1
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_text_2
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_title
import nl.ovfietsbeschikbaarheid.resources.about_title
import nl.ovfietsbeschikbaarheid.resources.content_description_back
import nl.ovfietsbeschikbaarheid.resources.pedal_bike_24px
import nl.ovfietsbeschikbaarheid.ui.theme.Blue90
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val BIKE_ICON_VIEWPORT_HEIGHT = 960
private const val BIKE_ICON_WHEELS_BOTTOM = 800
private val bikeSize = 112.dp
private val bikeIconPaddingBelowWheels = bikeSize * (BIKE_ICON_VIEWPORT_HEIGHT - BIKE_ICON_WHEELS_BOTTOM) / BIKE_ICON_VIEWPORT_HEIGHT

@Composable
fun AboutScreen(
    pricePer24Hours: String?,
    onBackClicked: () -> Unit
) {
    AboutView(
        pricePer24Hours,
        onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutView(
    pricePer24Hours: String?,
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
                        Text(stringResource(Res.string.about_title))
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RentHeader(pricePer24Hours)

                Column(
                    modifier = Modifier
                        .contentColumnWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(Res.string.about_ov_fiets_text_1))
                            withStyledLink("https://ns.nl/ov-fiets", "ns.nl/ov-fiets")
                            append(stringResource(Res.string.about_ov_fiets_text_2))
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Section(
                        title = stringResource(Res.string.about_lock_title),
                        text = buildAnnotatedString {
                            append(stringResource(Res.string.about_lock_text_1))
                            withStyledLink("https://ov-fiets.nl/slot", "ov-fiets.nl/slot")
                            append(stringResource(Res.string.about_lock_text_2))
                        }
                    )

                    Section(
                        title = stringResource(Res.string.about_app_title),
                        text = buildAnnotatedString {
                            append(stringResource(Res.string.about_app_text_1))
                            withStyledLink(
                                url = "https://github.com/cristan/OvFietsBeschikbaarheidApp",
                                text = stringResource(Res.string.about_app_text_1_on_github)
                            )
                            append(stringResource(Res.string.about_app_text_2))
                            withStyledLink(
                                url = "https://www.freepik.com/free-vector/map-white-background_4485469.htm",
                                text = stringResource(Res.string.about_app_text_3)
                            )
                            append(stringResource(Res.string.about_app_text_4))
                        }
                    )

                    ReviewCallToAction()
                }
            }
        }
    }
}

@Composable
private fun RentHeader(pricePer24Hours: String?) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clipToBounds(),
        contentAlignment = Alignment.TopCenter
    ) {
        val headerWidth = maxWidth
        Column(
            modifier = Modifier
                .contentColumnWidth()
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.about_ov_fiets_title),
                modifier = Modifier.semantics { heading() },
                color = Yellow50,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BikeRidingIntoView(rideInDistance = headerWidth)
                Text(
                    text = if (pricePer24Hours != null) {
                        stringResource(Res.string.about_ov_fiets_subtitle_with_amount, pricePer24Hours)
                    } else {
                        stringResource(Res.string.about_ov_fiets_subtitle_without_amount)
                    },
                    modifier = Modifier.padding(bottom = 20.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading)
                )
            }
        }
    }
}

@Composable
private fun BikeRidingIntoView(rideInDistance: Dp) {
    val rideInProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rideInProgress.animateTo(1f, tween(durationMillis = 1400, delayMillis = 300))
    }
    Icon(
        painter = painterResource(Res.drawable.pedal_bike_24px),
        contentDescription = null,
        tint = Yellow50,
        modifier = Modifier
            .size(bikeSize)
            .offset(y = bikeIconPaddingBelowWheels)
            .graphicsLayer { translationX = -rideInDistance.toPx() * (1f - rideInProgress.value) }
    )
}

@Composable
private fun Modifier.contentColumnWidth() =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .widthIn(max = 600.dp)

@Composable
private fun Section(title: String, text: AnnotatedString) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
internal fun ReviewButton(url: String, text: String) {
    val uriHandler = LocalUriHandler.current
    Surface(
        onClick = { uriHandler.openUri(url) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Yellow50,
        contentColor = Blue90
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
expect fun ReviewCallToAction()

@Preview
@Composable
fun AboutPreview() {
    AboutView(
        "4,65",
        onBackClicked = {}
    )
}