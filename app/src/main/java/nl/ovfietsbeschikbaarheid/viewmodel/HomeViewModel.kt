package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jordond.compass.permissions.PermissionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.usecase.FindNearbyLocationsUseCase
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import timber.log.Timber
import java.time.Duration
import java.time.Instant

class HomeViewModel(
    private val findNearbyLocationsUseCase: FindNearbyLocationsUseCase,
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper,
    private val locationLoader: LocationLoader
) : ViewModel() {

    private val _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    // The initial value doesn't really matter: it gets overwritten right away anyway
    private val _content = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)
    val content: State<HomeContent> = _content

    private var loadLocationsJob: Job? = null
    private var allLocationsResult: Result<List<LocationOverviewModel>>? = null

    private var loadGpsLocationJob: Job? = null
    private var showSearchTermJob: Job? = null
    private var geoCoderJob: Job? = null

    /**
     * Called when the screen is launched, but also when navigating back from the details screen.
     */
    fun onScreenLaunched() {
        Timber.d("screenLaunched called ${System.currentTimeMillis()}")
        if (content.value is HomeContent.InitialEmpty) {
            // Screen launched for the first time
            loadLocationsJob = viewModelScope.launch {
                allLocationsResult = overviewRepository.getResult()
            }
            loadLocation()
        } else {
            onReturnedToScreen()
        }
    }

    fun onReturnedToScreen() {
        Timber.d("onReturnedToScreen called")
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
                    && locationPermissionHelper.hasGpsPermission() -> {
                // The user went to the app settings and granted the location permission manually
                _content.value = HomeContent.Loading
                fetchLocation()
            }

            currentlyShown is HomeContent.GpsContent -> {
                // Do basically a pull to refresh when re-entering this screen when the data is 5 minutes or more old
                if (Duration.between(currentlyShown.fetchTime, Instant.now()).toMinutes() >= 5) {
                    val currentContent = _content.value
                    if (currentContent is HomeContent.GpsContent) {
                        _content.value = currentContent.copy(isRefreshing = true)
                        refresh()
                    }
                }
            }
        }
    }

    fun onRetryClicked() {
        _content.value = HomeContent.Loading
        refresh()
    }

    fun onPullToRefresh() {
        Timber.d("onPullToRefresh called")
        _content.value = (_content.value as HomeContent.GpsContent).copy(isRefreshing = true)
        // TODO: when this fails, show a snackbar instead of blocking the entire screen
        refresh()
    }

    private fun refresh() {
        loadLocationsJob = viewModelScope.launch {
            allLocationsResult = overviewRepository.getResult()
        }
        fetchLocation()
    }

    fun onTurnOnGpsClicked() {
        locationPermissionHelper.turnOnGps()
    }

    fun onRequestPermissionsClicked(currentState: AskPermissionState) {
        Timber.d("requestGpsPermissions called")

        if (currentState == AskPermissionState.DeniedPermanently) {
            locationPermissionHelper.openSettings()
        } else {
            viewModelScope.launch {
                requestPermission()
            }
        }
    }

    private suspend fun requestPermission() {
        val permissionState: PermissionState = locationPermissionHelper.requirePermission()
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

        // Stop all jobs which will update the screen: there is something else to show now.
        loadGpsLocationJob?.cancel()
        geoCoderJob?.cancel()
        showSearchTermJob?.cancel()

        if (searchTerm.isBlank()) {
            loadLocation()
        } else {
            // When you start typing while the location loading failed and we're not already loading the location, start loading the locations again
            if (!loadLocationsJob!!.isActive && allLocationsResult?.isFailure == true) {
                loadLocationsJob = viewModelScope.launch {
                    allLocationsResult = overviewRepository.getResult()
                }
            }

            // When you typed when we're still loading the locations, wait for it
            if (loadLocationsJob!!.isActive) {
                showSearchTermJob = viewModelScope.launch {
                    loadLocationsJob!!.join()
                    showSearchTerm(searchTerm)
                }
            } else {
                showSearchTerm(searchTerm)
            }
        }
    }

    private fun showSearchTerm(searchTerm: String) {
        val allLocations = allLocationsResult!!.getOrThrow()
        val filteredLocations = overviewRepository.getLocations(allLocations, searchTerm)
        val currentContent = _content.value
        if (currentContent is HomeContent.SearchTermContent) {
            // Update the search results right away, but keep the nearby locations and update them in another thread to avoid flicker
            _content.value = currentContent.copy(locations = filteredLocations)
        } else {
            _content.value = HomeContent.SearchTermContent(filteredLocations, searchTerm, nearbyLocations = null)
        }

        geoCoderJob = viewModelScope.launch {
            val nearbyLocations = findNearbyLocationsUseCase(searchTerm, allLocations)
            if (nearbyLocations == null && filteredLocations.isEmpty()) {
                _content.value = HomeContent.NoSearchResults(searchTerm)
            } else {
                _content.value = HomeContent.SearchTermContent(filteredLocations, searchTerm, nearbyLocations)
            }
        }
    }

    /**
     * Check for GPS turned on & the correct permission. If yes, load the location, otherwise show the appropriate warning.
     */
    private fun loadLocation() {
        if (!locationPermissionHelper.isGpsTurnedOn()) {
            _content.value = HomeContent.GpsTurnedOff
        } else if (!locationPermissionHelper.hasGpsPermission()) {
            val showRationale = locationPermissionHelper.shouldShowLocationRationale()
            val state = if (!showRationale) AskPermissionState.Initial else AskPermissionState.Denied
            _content.value = HomeContent.AskGpsPermission(state)
        } else {
            _content.value = HomeContent.Loading
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        loadGpsLocationJob = viewModelScope.launch {
            Timber.d("fetchLocation: Fetching location")

            val coordinates = locationLoader.loadCurrentCoordinates()
            if (coordinates == null) {
                _content.value = HomeContent.NoGpsLocation
            } else {
                loadLocationsJob?.join()

                val allLocations = allLocationsResult!!
                if (allLocations.isFailure) {
                    Timber.d(allLocations.exceptionOrNull())
                    _content.value = HomeContent.NetworkError
                } else {
                    val locationsWithDistance = LocationsMapper.withDistance(allLocations.getOrThrow(), coordinates)
                    _content.value = HomeContent.GpsContent(locationsWithDistance, Instant.now())
                }
            }
        }
    }
}