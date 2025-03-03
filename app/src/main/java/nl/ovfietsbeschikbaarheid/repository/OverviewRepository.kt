package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel

class OverviewRepository {
    private var lastResult: List<LocationOverviewModel>? = null

    private val httpClient = KtorApiClient()

    /**
     * Returns the last cached result.
     * In the off chance there is no cached result (which should only happen if the app ran out of memory),
     * the data is loaded again. This will throw an exception when there is no internet for example.
     */
    suspend fun getCachedOrLoad(): List<LocationOverviewModel> {
        lastResult?.let {
            return it
        }
        return getAllLocations()
    }

    suspend fun getAllLocations(): List<LocationOverviewModel> {
        val locations = LocationsMapper.map(httpClient.getLocations())
        lastResult = locations
        return locations
    }

    fun filterLocations(allLocations: List<LocationOverviewModel>, searchTerm: String): List<LocationOverviewModel> {
        return allLocations.filter { it.locationTitle.contains(searchTerm, ignoreCase = true) }
    }
}
