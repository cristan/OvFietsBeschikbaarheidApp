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
import kotlinx.coroutines.launch

private const val TAG = "LocationsViewModel"

class LocationsViewModel(
    private val geolocator: Geolocator,
    private val overviewRepository: OverviewRepository,
    private val locationPermissionHelper: LocationPermissionHelper
) : ViewModel() {

    private val _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    // The initial value doesn't really matter: it gets overwritten right away anyway
    private val _content = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)
    val content: State<HomeContent> = _content

    fun checkPermission() {
        Log.d(TAG, "checkPermission called")
        loadData(_searchTerm.value)
    }

    private var loadLocationJob: Job? = null

    fun fetchLocation() {
        _content.value = HomeContent.LoadingGpsLocation
        Log.d(TAG, "fetchLocation: Cancelling location job")
        loadLocationJob?.cancel()
        loadLocationJob = viewModelScope.launch {
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
                val locationsWithDistance = LocationsMapper.withDistance(overviewRepository.getAllLocations(), coordinates)
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
                if (locationPermissionHelper.shouldShowLocationRationale()) {
                    _content.value = HomeContent.AskForGpsPermission
                } else {
                    fetchLocation()
                }
            } else {
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

    data class GpsContent(val locations: List<LocationOverviewWithDistanceModel>) : HomeContent()

    data class SearchTermContent(val locations: List<LocationOverviewModel>) : HomeContent()

    data class NoSearchResults(val searchTerm: String) : HomeContent()
}