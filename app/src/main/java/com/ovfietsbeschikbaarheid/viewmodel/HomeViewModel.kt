package com.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovfietsbeschikbaarheid.mapper.LocationsMapper
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import com.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.hasPermission
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val geolocator: Geolocator,
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper
) : ViewModel() {

    private val _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    // The initial value doesn't really matter: it gets overwritten right away anyway
    private val _content = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)
    val content: State<HomeContent> = _content

    fun screenLaunched() {
        Timber.d("screenLaunched called")
        loadData(_searchTerm.value)
    }

    fun onReturnedToScreen() {
        val currentlyShown = _content.value
        if (currentlyShown is HomeContent.GpsTurnedOff) {
            // Check if the situation has changed: maybe the GPS is on now
            loadLocation()
        }
        val gpsContent = currentlyShown as? HomeContent.GpsContent
        gpsContent?.let {
            _content.value = gpsContent.copy(isRefreshing = true)
            fetchLocation()
        }
    }

    fun refreshGps() {
        Timber.d("refreshGps called")
        _content.value = (_content.value as HomeContent.GpsContent).copy(isRefreshing = true)
        fetchLocation()
    }

    fun onTurnOnGpsClicked() {
        locationPermissionHelper.turnOnGps()
    }

    fun onRequestPermissionsClicked(currentState: AskPermissionState) {
        Timber.d("requestGpsPermissions called")

        if (currentState == AskPermissionState.DeniedPermanently) {
            locationPermissionHelper.openAppSettings()
        } else {
            // Either the initial state (where we don't know whether permission is denied permanently)
            // or the denied once state, after which `geolocator.current()` will ask for permission again
            _content.value = HomeContent.LoadingGpsLocation
            fetchLocation()
        }
    }

    fun onSearchTermChanged(searchTerm: String) {
        this._searchTerm.value = searchTerm
        loadData(searchTerm)
    }

    private fun loadData(searchTerm: String) {
        if (searchTerm.isBlank()) {
            loadLocation()
        } else {
            val allLocations = overviewRepository.getAllLocations()
            val filteredLocations =
                allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
            if (filteredLocations.isNotEmpty()) {
                _content.value = HomeContent.SearchTermContent(filteredLocations)
            } else {
                _content.value = HomeContent.NoSearchResults(searchTerm)
            }
        }
    }

    private fun loadLocation() {
        if (!locationPermissionHelper.isGpsTurnedOn()) {
            _content.value = HomeContent.GpsTurnedOff
        } else if (!geolocator.hasPermission()) {
            val showRationale = locationPermissionHelper.shouldShowLocationRationale()
            val state = if (!showRationale) AskPermissionState.Initial else AskPermissionState.Denied
            _content.value = HomeContent.AskGpsPermission(state)
        } else {
            _content.value = HomeContent.LoadingGpsLocation
            fetchLocation()
        }
    }

    private var loadLocationJob: Job? = null
    private fun fetchLocation() {
        loadLocationJob?.let {
            Timber.d("fetchLocation: Cancelling location job")
            it.cancel()
        }
        loadLocationJob = viewModelScope.launch {
            Timber.d("fetchLocation: Fetching location")
            val geolocatorResult = geolocator.current()
            _content.value = geolocatorResult.toHomeContent()
        }
    }

    private fun GeolocatorResult.toHomeContent(): HomeContent {
        return when (this) {
            is GeolocatorResult.PermissionDenied -> {
                val state = if (forever) AskPermissionState.DeniedPermanently else AskPermissionState.Denied
                return HomeContent.AskGpsPermission(state)
            }
            is GeolocatorResult.NotSupported -> {
                return HomeContent.GpsTurnedOff
            }
            // Both are unexpected errors which shouldn't happen. Just show the error
            is GeolocatorResult.PermissionError, is GeolocatorResult.GeolocationFailed -> {
                return HomeContent.GpsError((this as GeolocatorResult.Error).message)
            }
            is GeolocatorResult.NotFound -> HomeContent.GpsError("Geen locatie gevonden")
            is GeolocatorResult.Success -> {
                val coordinates = data.coordinates
                val locationsWithDistance =
                    LocationsMapper.withDistance(overviewRepository.getAllLocations(), coordinates)
                return HomeContent.GpsContent(locationsWithDistance)
            }
            else -> throw Exception("Unexpected error: $this")
        }
    }
}

enum class AskPermissionState {
    Initial,
    Denied,
    DeniedPermanently
}

sealed class HomeContent {
    // Initial empty state while we set things up.
    data object InitialEmpty : HomeContent()

    data class AskGpsPermission(val state: AskPermissionState) : HomeContent()

    data object LoadingGpsLocation : HomeContent()

    data object GpsTurnedOff : HomeContent()

    data class GpsError(val message: String) : HomeContent()

    data class GpsContent(
        val locations: List<LocationOverviewWithDistanceModel>,
        val isRefreshing: Boolean = false
    ) : HomeContent()

    data class SearchTermContent(val locations: List<LocationOverviewModel>) : HomeContent()

    data class NoSearchResults(val searchTerm: String) : HomeContent()
}