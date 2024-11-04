package nl.ovfietsbeschikbaarheid.repository

import kotlinx.coroutines.flow.MutableStateFlow
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.state.ScreenState

class OverviewRepository {
    private val httpClient = KtorApiClient()

//    val allLocations: ScreenState<List<LocationOverviewModel>> = ScreenState.Loading

    val allLocations = MutableStateFlow<ScreenState<List<LocationOverviewModel>>>(ScreenState.Loading)

    suspend fun loadAllLocations() {
        // TODO: maybe a separate refresh method
        val currentState = allLocations.value
        if (currentState is ScreenState.Loaded) {
            allLocations.value = currentState.copy(isRefreshing = true)
        }
        try {
            val locations = httpClient.getLocations()
            val mapped = LocationsMapper.map(locations)
            allLocations.value = ScreenState.Loaded(mapped)
        } catch (e: Exception) {
            allLocations.value = ScreenState.Error
        }
    }

    // TODO: can throw an exception. Catch that and allow the user to retry (or use the cached locations)
    suspend fun getAllLocations(): List<LocationOverviewModel> {
        return LocationsMapper.map(httpClient.getLocations())
    }

    fun getLocations(allLocations: List<LocationOverviewModel>, searchTerm: String): List<LocationOverviewModel> {
        return allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
    }
}
