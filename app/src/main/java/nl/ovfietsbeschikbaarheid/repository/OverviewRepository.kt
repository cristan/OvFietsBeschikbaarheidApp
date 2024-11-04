package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.state.ScreenState
import java.time.Instant


class OverviewRepository {
    data class LocationsResult(val locations: List<LocationOverviewModel>, val fetchTime: Instant)

    private var lastResult: LocationsResult? = null

    private val httpClient = KtorApiClient()

    var allLocations: ScreenState<List<LocationOverviewModel>> = ScreenState.Loading

    suspend fun loadAllLocations() {
        // TODO: maybe a separate refresh method. Or maybe change the entire setup after we know what to do with refreshing
        //  In any case: the isRefreshing is not used right now.
        val currentState = allLocations
        if (currentState is ScreenState.Loaded) {
            allLocations = currentState.copy(isRefreshing = true)
        }
        try {
            val locations = httpClient.getLocations()
            val mapped = LocationsMapper.map(locations)
            allLocations = ScreenState.Loaded(mapped)
        } catch (e: Exception) {
            allLocations = ScreenState.Error
        }
    }

    /**
     * Returns the last cached result.
     * In the off chance there is no cached result (which should only happen if the app ran out of memory),
     * the data is loaded again. This will throw an exception when there is no internet for example.
     */
    suspend fun getCachedOrLoad(): LocationsResult {
        lastResult?.let {
            return it
        }
        val locations = httpClient.getLocations()
        val mapped = LocationsMapper.map(locations)
        val locationsResult = LocationsResult(mapped, Instant.now())
        lastResult = locationsResult
        return locationsResult
    }

    // TODO: can throw an exception. Catch that and allow the user to retry (or use the cached locations)
    suspend fun getAllLocations(): List<LocationOverviewModel> {
        return LocationsMapper.map(httpClient.getLocations())
    }

    fun getLocations(allLocations: List<LocationOverviewModel>, searchTerm: String): List<LocationOverviewModel> {
        return allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
    }
}
