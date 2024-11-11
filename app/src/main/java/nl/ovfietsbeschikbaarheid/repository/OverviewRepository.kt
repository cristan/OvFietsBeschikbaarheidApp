package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import java.time.Instant


class OverviewRepository {
    data class LocationsResult(val locations: List<LocationOverviewModel>, val fetchTime: Instant)

    private var lastResult: LocationsResult? = null

    private val httpClient = KtorApiClient()

    /**
     * Returns the last cached result.
     * In the off chance there is no cached result (which should only happen if the app ran out of memory),
     * the data is loaded again. This will throw an exception when there is no internet for example.
     * TODO: can throw
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

    suspend fun getResult(): Result<LocationsResult> {
        return try {
            Result.success(getCachedOrLoad())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllLocations(): List<LocationOverviewModel> {
        return LocationsMapper.map(httpClient.getLocations())
    }

    fun getLocations(allLocations: List<LocationOverviewModel>, searchTerm: String): List<LocationOverviewModel> {
        return allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
    }
}
