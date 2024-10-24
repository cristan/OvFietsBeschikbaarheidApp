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

class HomeViewModel(
    private val geocoder: Geocoder,
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper,
    private val locationLoader: LocationLoader
) : ViewModel() {

    val searchTerm: State<String>
        field = mutableStateOf("")

    val content: State<HomeContent>
        // The initial value doesn't really matter: it gets overwritten right away anyway
        field = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)

    private var geoCoderJob: Job? = null

    fun screenLaunched() {
        Timber.d("screenLaunched called")
        loadData(searchTerm.value)
    }

    fun onReturnedToScreen() {
        val currentlyShown = content.value
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
                    && LocationPermissionController.mobile().hasPermission() -> {
                // The user went to the app settings and granted the location permission manually
                content.value = HomeContent.LoadingGpsLocation
                fetchLocation()
            }

            currentlyShown is HomeContent.GpsContent -> {
                // Do basically a pull to refresh when re-entering this screen
                content.value = currentlyShown.copy(isRefreshing = true)
                fetchLocation()
            }
        }
    }

    fun refreshGps() {
        Timber.d("refreshGps called")
        content.value = (content.value as HomeContent.GpsContent).copy(isRefreshing = true)
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
                content.value = HomeContent.AskGpsPermission(AskPermissionState.Denied)
            }
            PermissionState.DeniedForever -> {
                content.value = HomeContent.AskGpsPermission(AskPermissionState.DeniedPermanently)
            }
            PermissionState.Granted -> {
                content.value = HomeContent.LoadingGpsLocation
                fetchLocation()
            }
        }
    }

    fun onSearchTermChanged(searchTerm: String) {
        this.searchTerm.value = searchTerm
        loadData(searchTerm)
    }

    private fun loadData(searchTerm: String) {
        if (searchTerm.isBlank()) {
            loadLocation()
        } else {
            val filteredLocations = overviewRepository.getLocations(searchTerm)
            val currentContent = content.value
            if (currentContent is HomeContent.SearchTermContent) {
                // Update the search results right away, but keep the nearby locations and update them in another thread to avoid flicker
                content.value = currentContent.copy(locations = filteredLocations)
            } else {
                content.value = HomeContent.SearchTermContent(filteredLocations, searchTerm, nearbyLocations = null)
            }

            geoCoderJob?.cancel()
            geoCoderJob = viewModelScope.launch {
                val nearbyLocations = findNearbyLocations(searchTerm)
                if (nearbyLocations == null && filteredLocations.isEmpty()) {
                    content.value = HomeContent.NoSearchResults(searchTerm)
                } else {
                    content.value = HomeContent.SearchTermContent(filteredLocations, searchTerm, nearbyLocations)
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

    /**
     * Check for GPS turned on & the correct permission. If yes, load the location, otherwise show the appropriate warning.
     */
    private fun loadLocation() {
        if (!locationPermissionHelper.isGpsTurnedOn()) {
            content.value = HomeContent.GpsTurnedOff
        } else if (!LocationPermissionController.mobile().hasPermission()) {
            val showRationale = locationPermissionHelper.shouldShowLocationRationale()
            val state = if (!showRationale) AskPermissionState.Initial else AskPermissionState.Denied
            content.value = HomeContent.AskGpsPermission(state)
        } else {
            content.value = HomeContent.LoadingGpsLocation
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

            val coordinates = locationLoader.loadCurrentCoordinates()
            if (coordinates == null) {
                content.value = HomeContent.GpsError("Geen locatie gevonden")
            } else {
                val locationsWithDistance =
                    LocationsMapper.withDistance(overviewRepository.getAllLocations(), coordinates)
                content.value = HomeContent.GpsContent(locationsWithDistance)
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