package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.model.VehicleModel
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.ui.view.FullPageLoader
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
    screenState: ScreenState<List<VehicleModel>>,
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
                is ScreenState.Loaded<List<VehicleModel>> -> {
                    ActualMap(innerPadding, screenState.data)
                }
            }
        }
    }
}

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun ActualMap(
    innerPadding: PaddingValues,
    vehicles: List<VehicleModel>,
) {
    Surface(
        Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        val cameraPositionState = rememberCameraPositionState {
            // TODO: zoom in a way that you can see all OV-Fiets locations
            position = CameraPosition.fromLatLngZoom(LatLng(52.2129919, 5.2793703), 10f)
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            // TODO: causes a crash when the user hasn't given location permission
            properties = MapProperties(isMyLocationEnabled = true),
            googleMapOptionsFactory = { GoogleMapOptions().mapColorScheme(MapColorScheme.FOLLOW_SYSTEM) }
        ) {
            // TODO: always show OV-fiets locations
            Clustering(
                items = vehicles,
                // Optional: Handle clicks on clusters, cluster items, and cluster item info windows
                onClusterClick = {
                    Timber.d("Cluster clicked! $it")
                    false
                },
                onClusterItemClick = {
                    Timber.d("Cluster item clicked! $it")
                    false
                },
                onClusterItemInfoWindowClick = {
                    Timber.d("Cluster item info window clicked! $it")
                },
                // Optional: Custom rendering for non-clustered items
                clusterItemContent = null
            )
        }
    }
}