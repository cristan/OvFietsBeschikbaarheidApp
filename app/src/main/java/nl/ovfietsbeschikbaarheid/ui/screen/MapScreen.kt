package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapColorScheme
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.clustering.rememberClusterManager
import com.google.maps.android.compose.clustering.rememberClusterRenderer
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.ktx.utils.sphericalDistance
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.VehicleModel
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.ui.view.FullPageLoader
import nl.ovfietsbeschikbaarheid.viewmodel.MapContent
import nl.ovfietsbeschikbaarheid.viewmodel.MapViewModel
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun MapScreen(
    viewModel: MapViewModel = koinViewModel(),
    onBackClicked: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.screenLaunched()
    }

    val screenState by viewModel.screenState

    MapView(
        screenState,
        viewModel::onRetryClick,
        onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapView(
    screenState: ScreenState<MapContent>,
    onRetry: () -> Unit,
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
                        Text("Kaart")
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
            when (screenState) {
                ScreenState.FullPageError -> FullPageError(onRetry = onRetry)
                ScreenState.Loading -> FullPageLoader()
                is ScreenState.Loaded<MapContent> -> {
                    ActualMap(innerPadding, screenState.data.vehicles, screenState.data.overviewModels)
                }
            }
        }
    }
}

@Composable
private fun ActualMap(
    innerPadding: PaddingValues,
    vehicles: List<VehicleModel>,
    locationOverviewModels: List<LocationOverviewModel>
) {
    Surface(
        Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        val coroutineScope = rememberCoroutineScope()
        val cameraPositionState = rememberCameraPositionState {
            // TODO: zoom in a way that you can see all OV-Fiets locations
            //  Alternatively, zoom into your own location when you have location permission
            position = CameraPosition.fromLatLngZoom(LatLng(52.2129919, 5.2793703), 10f)
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            // TODO: causes a crash when the user hasn't given location permission
            properties = MapProperties(isMyLocationEnabled = true),
            googleMapOptionsFactory = { GoogleMapOptions().mapColorScheme(MapColorScheme.FOLLOW_SYSTEM) }
        ) {
            locationOverviewModels.forEach {
                Marker(
                    icon = defaultMarker(54f),
                    state = rememberMarkerState(position = LatLng(it.latitude, it.longitude)),
                    title = it.title,
                    snippet = stringResource(R.string.map_available, it.rentalBikesAvailable?.toString() ?: "??")
                )
            }

            MyCustomRendererClustering(vehicles, { cameraUpdate ->
                coroutineScope.launch {
                    cameraPositionState.animate(cameraUpdate)
                }
            })
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyCustomRendererClustering(items: List<VehicleModel>, animate: (CameraUpdate) -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val clusterManager = rememberClusterManager<VehicleModel>()

    // Using the NonHierarchicalViewBasedAlgorithm speeds up rendering
    clusterManager?.setAlgorithm(
        NonHierarchicalViewBasedAlgorithm(
            screenWidth.value.toInt(),
            screenHeight.value.toInt()
        )
    )
    val renderer = rememberClusterRenderer(
        clusterContent = { cluster ->
            val averageColor = cluster.items.map { it.getColor() }
                .let { colors ->
                    val size = colors.size
                    val allReds = colors.sumOf { it.red } / size
                    val avgGreen = colors.sumOf { it.green } / size
                    val avgBlue = colors.sumOf { it.blue } / size
                    Color(allReds, avgGreen, avgBlue)
                }

            CircleContent(
                modifier = Modifier.size(40.dp),
                text = "%,d".format(cluster.size),
                color = averageColor,
            )
        },
        clusterItemContent = {
            CircleContent(
                modifier = Modifier.size(20.dp),
                text = "",
                color = it.getColor(),
            )
        },
        clusterManager = clusterManager,
    )

    val shownVehicleModels = remember { mutableStateOf<List<VehicleModel>?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val shownVehicleModelsValue = shownVehicleModels.value
    if(shownVehicleModelsValue != null) {
        ModalBottomSheet(
            onDismissRequest = {
                shownVehicleModels.value = null
            },
            sheetState = sheetState
        ) {
            LazyColumn {
                items(shownVehicleModelsValue) { shownVehicleModel ->
                    Text(shownVehicleModel.title)
                    Text(shownVehicleModel.snippet)
                }
            }
        }
    }

    SideEffect {
        clusterManager ?: return@SideEffect
        clusterManager.setOnClusterClickListener { cluster ->
            val bounds = LatLngBounds.builder().apply {
                cluster.items.forEach { include(it.position) }
            }.build()
            val distance = bounds.northeast.sphericalDistance(bounds.southwest)
            Timber.d("distance: $distance")
            if(distance < 5) {
                shownVehicleModels.value = cluster.items.toList()
                return@setOnClusterClickListener true
            }

            val newLatLngBounds: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            animate(newLatLngBounds)

            true // Return true to indicate we handled the click
        }
        clusterManager.setOnClusterItemClickListener {
            Timber.d( "Cluster item clicked! $it")
            shownVehicleModels.value = listOf(it)
            true
        }
    }
    SideEffect {
        if (clusterManager?.renderer != renderer) {
            clusterManager?.renderer = renderer ?: return@SideEffect
        }
    }

    if (clusterManager != null) {
        Clustering(
            items = items,
            clusterManager = clusterManager,
        )
    }
}

@Composable
private fun CircleContent(
    color: Color,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier,
        shape = CircleShape,
        color = color,
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}