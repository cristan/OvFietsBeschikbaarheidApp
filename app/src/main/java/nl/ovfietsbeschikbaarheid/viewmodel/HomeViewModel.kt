package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.compass.Priority
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.permissions.LocationPermissionController
import dev.jordond.compass.permissions.PermissionState
import dev.jordond.compass.permissions.mobile
import dev.jordond.compass.permissions.mobile.openSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.ext.isInTheNetherlands
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import timber.log.Timber
import java.time.Duration
import java.time.Instant

class HomeViewModel(
    private val geocoder: Geocoder,
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper,
    private val locationLoader: LocationLoader
) : ViewModel() {

    private val _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    // The initial value doesn't really matter: it gets overwritten right away anyway
    private val _content = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)
    val content: State<HomeContent> = _content

    private var geoCoderJob: Job? = null
    private var loadLocationsJob: Job? = null
    private var allLocationsResult: Result<OverviewRepository.LocationsResult>? = null

    fun screenLaunched() {
        Timber.d("screenLaunched called")
        if (loadLocationsJob == null) {
            loadLocationsJob = viewModelScope.launch {
                allLocationsResult = overviewRepository.getResult()
            }
        }
        loadData(_searchTerm.value)
    }

    fun onReturnedToScreen() {
        val currentlyShown = _content.value
        when {
            currentlyShown is HomeContent.GpsTurnedOff && locationPermissionHelper.isGpsTurnedOn() -> {
                // The GPS is on now
                loadLocation()
            }

            currentlyShown is HomeContent.NoGpsLocation -> {
                // Let's try again
                loadLocation()
            }

            currentlyShown is HomeContent.AskGpsPermission
                    && currentlyShown.state == AskPermissionState.DeniedPermanently
                    && LocationPermissionController.mobile().hasPermission() -> {
                // The user went to the app settings and granted the location permission manually
                _content.value = HomeContent.Loading
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
            viewModelScope.launch {
                requestPermission()
            }
        }
    }

    private suspend fun requestPermission() {
        val permissionState: PermissionState = LocationPermissionController.mobile().requirePermissionFor(Priority.Balanced)
        when (permissionState) {
            // Doesn't happen on Android
            PermissionState.NotDetermined -> Unit
            PermissionState.Denied -> {
                _content.value = HomeContent.AskGpsPermission(AskPermissionState.Denied)
            }
            PermissionState.DeniedForever -> {
                _content.value = HomeContent.AskGpsPermission(AskPermissionState.DeniedPermanently)
            }
            PermissionState.Granted -> {
                _content.value = HomeContent.Loading
                fetchLocation()
            }
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
            // TODO: handle the job not being completed yet or having failed
            val allLocations = allLocationsResult!!.getOrThrow().locations
            val filteredLocations = overviewRepository.getLocations(allLocations, searchTerm)
            val currentContent = _content.value
            if (currentContent is HomeContent.SearchTermContent) {
                // Update the search results right away, but keep the nearby locations and update them in another thread to avoid flicker
                _content.value = currentContent.copy(locations = filteredLocations)
            } else {
                _content.value = HomeContent.SearchTermContent(filteredLocations, searchTerm, nearbyLocations = null)
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
            // TODO: load when more than 1? minute old and also catch exception
            LocationsMapper.withDistance(allLocationsResult!!.getOrThrow().locations, foundCoordinates)
        } else {
            null
        }
    }

    /**
     * Check for GPS turned on & the correct permission. If yes, load the location, otherwise show the appropriate warning.
     */
    private fun loadLocation() {
        if (!locationPermissionHelper.isGpsTurnedOn()) {
            _content.value = HomeContent.GpsTurnedOff
        } else if (!LocationPermissionController.mobile().hasPermission()) {
            val showRationale = locationPermissionHelper.shouldShowLocationRationale()
            val state = if (!showRationale) AskPermissionState.Initial else AskPermissionState.Denied
            _content.value = HomeContent.AskGpsPermission(state)
        } else {
            _content.value = HomeContent.Loading
            fetchLocation()
        }
    }

    private var loadGpsLocationJob: Job? = null
    private fun fetchLocation() {
        loadGpsLocationJob?.let {
            Timber.d("fetchLocation: Cancelling location job")
            it.cancel()
        }
        loadGpsLocationJob = viewModelScope.launch {
            Timber.d("fetchLocation: Fetching location")

            val coordinates = locationLoader.loadCurrentCoordinates()
            if (coordinates == null) {
                _content.value = HomeContent.NoGpsLocation
            } else {
                loadLocationsJob?.join()

                // Load the locations again if they're too old
                allLocationsResult!!.getOrNull()?.let {
                    val lastFetchTime = it.fetchTime
                    val now = Instant.now()

                    if (Duration.between(lastFetchTime, now).seconds > 60) {
                        allLocationsResult = overviewRepository.getResult()
                    }
                }

                val allLocations = allLocationsResult!!
                if (allLocations.isFailure) {
                    // TODO: handle. This should probably be fixed first: this 100% needs to happen and maybe makes other things easier
                    error("Loading error! Not handled yet")
                } else {
                    val locationsWithDistance = LocationsMapper.withDistance(allLocations.getOrThrow().locations, coordinates)
                    _content.value = HomeContent.GpsContent(locationsWithDistance)
                }
            }
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

    data object Loading : HomeContent()

    data object GpsTurnedOff : HomeContent()

    data object NoGpsLocation: HomeContent()

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