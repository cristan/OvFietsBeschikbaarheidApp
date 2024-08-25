package com.ovfietsbeschikbaarheid.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.hasPermission
import dev.jordond.compass.geolocation.mobile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val TAG = "LocationsViewModel"

class LocationsViewModel(
    private val overviewRepository: OverviewRepository
) : ViewModel() {

    private val _searchTerm = mutableStateOf("")
    val searchTerm: State<String> = _searchTerm

    // The initial value doesn't really matter: it gets overwritten right away anyway
    private val _content = mutableStateOf<HomeContent>(HomeContent.InitialEmpty)
    val content: State<HomeContent> = _content

    private val geolocator: Geolocator = Geolocator.mobile()

    fun checkPermission() {
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
                return HomeContent.GpsError(geolocatorResult.message)
            }

            is GeolocatorResult.Success -> {
                val coordinates = geolocatorResult.data.coordinates
                val locationsWithDistance = overviewRepository.getLocationsWithDistance(coordinates)
                return HomeContent.GpsContent(locationsWithDistance)
            }
        }
    }

    fun onSearchTermChanged(searchTerm: String) {
        this._searchTerm.value = searchTerm
        loadData(searchTerm)
    }

    private fun loadData(searchTerm: String) {
        if (searchTerm.isBlank()) {
            if (!geolocator.hasPermission()) {
                _content.value = HomeContent.AskForGpsPermission
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
    // Initial empty state while we set things up
    data object InitialEmpty : HomeContent()

    data object AskForGpsPermission : HomeContent()

    data object LoadingGpsLocation : HomeContent()

    // TODO: split into the possible values of GeolocatorResult
    data class GpsError(val message: String) : HomeContent()

    data class GpsContent(val locations: List<LocationOverviewWithDistanceModel>) : HomeContent()

    data class SearchTermContent(val locations: List<LocationOverviewModel>) : HomeContent()

    data class NoSearchResults(val searchTerm: String) : HomeContent()
}