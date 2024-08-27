package com.ovfietsbeschikbaarheid.viewmodel

import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

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

    fun onReturnedToScreen() {
        val gpsContent = _content.value as? HomeContent.GpsContent
        gpsContent?.let {
            _content.value = gpsContent.copy(isRefreshing = true)
            fetchLocation()
        }
    }

    fun refreshGps() {
        _content.value = (_content.value as HomeContent.GpsContent).copy(isRefreshing = true)
        Log.d(TAG, "refreshGps called")
        fetchLocation()
    }

    fun screenLaunched() {
        Log.d(TAG, "screenLaunched called")
        loadData(_searchTerm.value)
    }

    fun requestGpsPermissions() {
        Log.d(TAG, "requestGpsPermissions called")
        _content.value = HomeContent.LoadingGpsLocation
        fetchLocation()
    }

    private var loadLocationJob: Job? = null
    private fun fetchLocation() {
        loadLocationJob?.let {
            Log.d(TAG, "fetchLocation: Cancelling location job")
            it.cancel()
        }
        loadLocationJob = viewModelScope.launch {
            delay(1000)
            val geolocatorResult = geolocator.current()
            _content.value = getGpsContent(geolocatorResult)
        }
    }

    private fun getGpsContent(geolocatorResult: GeolocatorResult): HomeContent {
        when (geolocatorResult) {
            is GeolocatorResult.Error -> {
                val message = getErrorMessage(geolocatorResult)
                return HomeContent.GpsError(message)
            }

            is GeolocatorResult.Success -> {
                val coordinates = geolocatorResult.data.coordinates
                val locationsWithDistance =
                    LocationsMapper.withDistance(overviewRepository.getAllLocations(), coordinates)
                return HomeContent.GpsContent(locationsWithDistance)
            }
        }
    }

    private fun getErrorMessage(error: GeolocatorResult.Error): String {
        val message = when (error) {
            GeolocatorResult.NotFound -> "Geen locatie gevonden"
            GeolocatorResult.NotSupported -> "GPS moet aanstaan om OV fietsen in de buurt te vinden."
            is GeolocatorResult.GeolocationFailed -> {
                Log.w(TAG, "Geo location failed: ${error.message}")
                "Locatie ophalen mislukt"
            }

            is GeolocatorResult.PermissionError -> {
                Log.w(TAG, "Permission error: ${error.message}")
                "Locatie ophalen mislukt"
            }

            is GeolocatorResult.PermissionDenied -> {
                if (error.forever) {
                    "Geef de app toegang tot je locatie om OV fietsen in je buurt te zien."
                } else {
                    "De app heeft toestemming nodig om OV fietsen in de buurt te laten zien."
                }
            }
            // This is a limitation of the smart cast: all cases of GeolocatorResult.Error have been covered
            else -> throw Exception("Unexpected error: $error")
        }
        return message
    }

    fun onSearchTermChanged(searchTerm: String) {
        this._searchTerm.value = searchTerm
        loadData(searchTerm)
    }

    private fun loadData(searchTerm: String) {
        if (searchTerm.isBlank()) {
            if (!geolocator.hasPermission()) {
                val shouldShowLocationRationale = locationPermissionHelper.shouldShowLocationRationale()
                if (shouldShowLocationRationale) {
                    _content.value = HomeContent.AskForGpsPermission
                } else {
                    _content.value = HomeContent.LoadingGpsLocation
                    fetchLocation()
                }
            } else {
                _content.value = HomeContent.LoadingGpsLocation
                fetchLocation()
            }
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
}

sealed class HomeContent {
    // Initial empty state while we set things up.
    data object InitialEmpty : HomeContent()

    data object AskForGpsPermission : HomeContent()

    data object LoadingGpsLocation : HomeContent()

    data class GpsError(val message: String) : HomeContent()

    data class GpsContent(
        val locations: List<LocationOverviewWithDistanceModel>,
        val isRefreshing: Boolean = false
    ) : HomeContent()

    data class SearchTermContent(val locations: List<LocationOverviewModel>) : HomeContent()

    data class NoSearchResults(val searchTerm: String) : HomeContent()
}