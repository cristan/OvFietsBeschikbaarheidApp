package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import java.time.Instant


class OverviewRepository {
    data class LocationsResult(val locations: List<LocationOverviewModel>, val fetchTime: Instant)

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
        return loadLocations()
    }

    private suspend fun loadLocations(): List<LocationOverviewModel> {
        val locations = httpClient.getLocations()
        val mapped = LocationsMapper.map(locations)
        lastResult = mapped
        return mapped
    }

    suspend fun getResult(): Result<List<LocationOverviewModel>> {
        return try {
            Result.success(loadLocations())
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
