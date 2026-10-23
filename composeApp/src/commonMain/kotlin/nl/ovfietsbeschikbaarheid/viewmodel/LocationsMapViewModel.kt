package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.jordond.compass.Coordinates
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import nl.ovfietsbeschikbaarheid.ext.isInTheNetherlands
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper

class LocationsMapViewModel(
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper,
    private val locationLoader: LocationLoader,
) : ViewModel() {

    val screenState: State<ScreenState<LocationsMapContent>>
        field = mutableStateOf<ScreenState<LocationsMapContent>>(ScreenState.Loading)

    init {
        load()
    }

    fun onRetryClick() {
        screenState.value = ScreenState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val overviewData = overviewRepository.getCachedOrLoad()
                val hasLocationPermission = locationPermissionHelper.hasGpsPermission()
                val userCoordinates = if (hasLocationPermission) {
                    locationLoader.getLastKnownCoordinates()?.takeIf { it.isInTheNetherlands() }
                } else {
                    null
                }
                screenState.value = ScreenState.Loaded(
                    LocationsMapContent(overviewData.locations, userCoordinates, showsUserLocation = hasLocationPermission)
                )
            } catch (e: ResponseException) {
                Logger.e(e) { "Error fetching the locations for the map" }
                screenState.value = ScreenState.FullPageError
            } catch (e: IOException) {
                Logger.e(e) { "Error fetching the locations for the map" }
                screenState.value = ScreenState.FullPageError
            }
        }
    }
}

data class LocationsMapContent(
    val locations: List<LocationOverviewModel>,
    val userCoordinates: Coordinates?,
    val showsUserLocation: Boolean,
)
