package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.jordond.compass.Coordinates
import dev.jordond.compass.permissions.PermissionState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import nl.ovfietsbeschikbaarheid.ext.tryAwait
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OverviewDataModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.usecase.FindNearbyLocationsUseCase
import nl.ovfietsbeschikbaarheid.util.InAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import nl.ovfietsbeschikbaarheid.util.RatingEligibilityService
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class HomeViewModel(
    private val findNearbyLocationsUseCase: FindNearbyLocationsUseCase,
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper,
    private val locationLoader: LocationLoader,
    private val locationsMapper: LocationsMapper,
    private val ratingEligibilityService: RatingEligibilityService,
    private val inAppReviewProvider: InAppReviewProvider
) : ViewModel() {

    private val _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    // The initial value doesn't really matter: it gets overwritten right away anyway
    private val _content = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)
    val content: State<HomeContent> = _content

    private val _pricePer24Hours = mutableStateOf<String?>(null)
    val pricePer24Hours: State<String?> = _pricePer24Hours

    private lateinit var overviewData: Deferred<OverviewDataModel>
    private var lastLoadedCoordinates: Coordinates? = null

    private var loadGpsLocationJob: Job? = null
    private var showSearchTermJob: Job? = null
    private var geoCoderJob: Job? = null

    /**
     * Called when the screen is launched, but also when navigating back from the details screen.
     */
    fun onScreenLaunched() {
        Logger.d("screenLaunched called ${Clock.System.now().toEpochMilliseconds()}")
        if (content.value is HomeContent.InitialEmpty) {
            // Screen launched for the first time
            overviewData = viewModelScope.async {
                overviewRepository.getOverviewData()
            }
            loadLocation()
        } else {
            onReturnedToScreen()
        }
    }

    fun onReturnedToScreen(now: Instant = Clock.System.now()) {
        Logger.d("onReturnedToScreen called")
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

            currentlyShown is HomeContent.AskGpsPermission && locationPermissionHelper.hasGpsPermission() -> {
                // The user went to the app settings and granted the location permission manually
                // On iOS, this is the default flow: when you grant permission, we don't get feedback right away
                //  and we just have permissions when we get back
                _content.value = HomeContent.Loading
                awaitAndShowLocationsWithDistance()
            }

            currentlyShown is HomeContent.GpsContent -> {
                // Do basically a pull to refresh when re-entering this screen when the data is 5 minutes or more old
                val inWholeMinutes = (now - currentlyShown.fetchTime).inWholeMinutes
                if (inWholeMinutes >= 5) {
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
        Logger.d("onPullToRefresh called")
        _content.value = (_content.value as HomeContent.GpsContent).copy(isRefreshing = true)
        // TODO: when this fails, show a snackbar instead of blocking the entire screen
        refresh()
    }

    private fun refresh() {
        overviewData = viewModelScope.async {
            overviewRepository.getOverviewData()
        }
        lastLoadedCoordinates = null
        awaitAndShowLocationsWithDistance()
    }

    fun onTurnOnGpsClicked() {
        locationPermissionHelper.turnOnGps()
    }

    fun onRequestPermissionsClicked(currentState: AskPermissionState) {
        Logger.d("requestGpsPermissions called")

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
            // iOS only: happens after requesting permission. We don't get an answer right away, so we'll do nothing and check again when we return.
            PermissionState.NotDetermined -> Unit

            PermissionState.Denied -> {
                _content.value = HomeContent.AskGpsPermission(AskPermissionState.Denied)
            }

            PermissionState.DeniedForever -> {
                _content.value = HomeContent.AskGpsPermission(AskPermissionState.DeniedPermanently)
            }

            PermissionState.Granted -> {
                _content.value = HomeContent.Loading
                awaitAndShowLocationsWithDistance()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onSearchTermChanged(searchTerm: String) {
        this._searchTerm.value = searchTerm

        // Stop all jobs which will update the screen: there is something else to show now.
        loadGpsLocationJob?.cancel()
        geoCoderJob?.cancel()
        showSearchTermJob?.cancel()

        // When you start typing while the location loading failed and we're not already loading the location, start loading the locations again
        if (overviewData.isCompleted && overviewData.getCompletionExceptionOrNull() != null) {
            overviewData = viewModelScope.async {
                overviewRepository.getOverviewData()
            }
        }

        if (searchTerm.isBlank()) {
            loadLocation()
        } else {
            showSearchTermJob = viewModelScope.launch {
                try {
                    val loadedOverviewData = overviewData.await()
                    _pricePer24Hours.value = loadedOverviewData.pricePer24Hours
                    showSearchTerm(searchTerm, loadedOverviewData.locations)
                } catch (e: Exception) {
                    Logger.e(e) { "onSearchTermChanged: Failed to fetch locations." }
                    _content.value = HomeContent.NetworkError
                }
            }
        }
    }

    private fun showSearchTerm(searchTerm: String, allLocations: List<LocationOverviewModel>) {
        val filteredLocations = overviewRepository.filterLocations(allLocations, searchTerm)
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
            fetchPriceDataIndependently()
        } else if (!locationPermissionHelper.hasGpsPermission()) {
            val showRationale = locationPermissionHelper.shouldShowLocationRationale()
            val state = if (!showRationale){
                AskPermissionState.Initial
            } else {
                if(locationPermissionHelper.isDeniedPermanently()) {
                    AskPermissionState.DeniedPermanently
                } else {
                    AskPermissionState.Denied
                }
            }
            _content.value = HomeContent.AskGpsPermission(state)
            fetchPriceDataIndependently()
        } else {
            _content.value = HomeContent.Loading
            awaitAndShowLocationsWithDistance()
        }
    }

    private fun fetchPriceDataIndependently() {
        viewModelScope.launch {
            try {
                val loadedOverviewData = overviewData.await()
                _pricePer24Hours.value = loadedOverviewData.pricePer24Hours
            } catch (e: Exception) {
                // Whenever this fails, we just don't know the price per 24 hours.
                // That's fine for now, we won't bother the user that their internet apparently doesn't work until they approve loading GPS locations
                Logger.e(e) { "fetchPriceDataIndependently: Failed to fetch overview data for price." }
            }
        }
    }

    private fun awaitAndShowLocationsWithDistance() {
        loadGpsLocationJob = viewModelScope.launch {
            Logger.d("awaitAndShowLocationsWithDistance: Fetching location")

            try {
                // Load the locations and coordinates in parallel
                val coordinatesDeferred = async {
                    lastLoadedCoordinates ?: locationLoader.loadCurrentCoordinates()
                }
                Logger.d("awaitAndShowLocationsWithDistance: awaiting locations")
                val loadedOverviewData = overviewData.await()
                _pricePer24Hours.value = loadedOverviewData.pricePer24Hours

                // The locations have loaded. When we're not already showing locations by distance...
                if (_content.value !is HomeContent.GpsContent) {
                    // ...try if the coordinates resolve within 5 ms...
                    val fastCoordinates = coordinatesDeferred.tryAwait(timeoutMillis = 5)
                    if (fastCoordinates == null) {
                        // ... if no, show the last known coordinates while the coordinates are loading
                        // with isRefreshing = true while the coordinates continue loading.
                        val lastKnownCoordinates = locationLoader.getLastKnownCoordinates()
                        if (lastKnownCoordinates != null) {
                            val locationsWithDistance = locationsMapper.withDistance(loadedOverviewData.locations, lastKnownCoordinates)
                            Logger.d("awaitAndShowLocationsWithDistance: using last known coordinates")
                            _content.value = HomeContent.GpsContent(locationsWithDistance, Clock.System.now(), isRefreshing = true)
                        }
                    }
                }

                val loadedCoordinates = coordinatesDeferred.await()
                lastLoadedCoordinates = loadedCoordinates

                if (loadedCoordinates == null) {
                    _content.value = HomeContent.NoGpsLocation
                } else {
                    Logger.d("awaitAndShowLocationsWithDistance: using loaded coordinates")
                    val locationsWithDistance = locationsMapper.withDistance(loadedOverviewData.locations, loadedCoordinates)
                    _content.value = HomeContent.GpsContent(locationsWithDistance, Clock.System.now())

                    ratingEligibilityService.onGpsContentViewed()
                    if (ratingEligibilityService.shouldRequestRating()) {
                        inAppReviewProvider.invokeAppReview()
                    }
                }
            } catch (_: CancellationException) {
                // The job got cancelled. That's fine: the new job will show the user what they want.
            } catch (e: IOException) {
                Logger.e(e) { "fetchLocation: Failed to fetch location" }
                _content.value = HomeContent.NetworkError
            }
        }
    }
}