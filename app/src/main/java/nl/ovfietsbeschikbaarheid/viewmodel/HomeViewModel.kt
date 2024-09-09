package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.hasPermission
import dev.jordond.compass.permissions.LocationPermissionController
import dev.jordond.compass.permissions.mobile.openSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.ext.isInTheNetherlands
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import timber.log.Timber

class HomeViewModel(
    private val geolocator: Geolocator,
    private val geocoder: Geocoder,
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper
) : ViewModel() {

    private val _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    // The initial value doesn't really matter: it gets overwritten right away anyway
    private val _content = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)
    val content: State<HomeContent> = _content

    private var geoCoderJob: Job? = null

    fun screenLaunched() {
        Timber.d("screenLaunched called")
        loadData(_searchTerm.value)
    }

    fun onReturnedToScreen() {
        val currentlyShown = _content.value
        when {
            currentlyShown is HomeContent.GpsTurnedOff && locationPermissionHelper.isGpsTurnedOn() -> {
                // The GPS is on now
                loadLocation()
            }

            currentlyShown is HomeContent.GpsError -> {
                // Let's try again
                loadLocation()
            }

            currentlyShown is HomeContent.AskGpsPermission
                    && currentlyShown.state == AskPermissionState.DeniedPermanently
                    && geolocator.hasPermission() -> {
                // The user went to the app settings and granted the location permission manually
                _content.value = HomeContent.LoadingGpsLocation
                fetchLocation()
            }

            currentlyShown is HomeContent.GpsContent -> {
                // Do basically a pull to refresh when re-entering this screen
                _content.value = currentlyShown.copy(isRefreshing = true)
                fetchLocation()
            }
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
            LocationPermissionController.openSettings()
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
            val filteredLocations = allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
            val currentContent = _content.value
            if (currentContent is HomeContent.SearchTermContent) {
                // Update the search results right away, but load the nearby locations in another thread to avoid flicker
                _content.value = currentContent.copy(locations = filteredLocations)
            }

            geoCoderJob?.cancel()
            geoCoderJob = viewModelScope.launch {
                val nearbyLocations = findNearbyLocations(searchTerm)
                if (nearbyLocations == null && filteredLocations.isEmpty()) {
                    _content.value = HomeContent.NoSearchResults(searchTerm)
                } else {
                    _content.value = HomeContent.SearchTermContent(filteredLocations, searchTerm, nearbyLocations)
                }
            }
        }
    }

    private suspend fun findNearbyLocations(searchTerm: String): List<LocationOverviewWithDistanceModel>? {
        val geoCoderAvailable = geocoder.isAvailable()
        if (!geoCoderAvailable) {
            return null
        }

        val coordinates = geocoder.forward(searchTerm).getOrNull()

        return if (coordinates != null) {
            val foundCoordinates = coordinates.find { it.isInTheNetherlands() } ?: coordinates[0]
            LocationsMapper.withDistance(overviewRepository.getAllLocations(), foundCoordinates)
        } else {
            null
        }
    }

    private fun loadLocation() {
        // TODO: replace with geolocator.isAvailable() when this is no longer a suspend fun: https://github.com/jordond/compass/issues/101
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

    data class SearchTermContent(
        val locations: List<LocationOverviewModel>,
        val searchTerm: String,
        val nearbyLocations: List<LocationOverviewWithDistanceModel>?
    ) : HomeContent()

    data class NoSearchResults(val searchTerm: String) : HomeContent()
}