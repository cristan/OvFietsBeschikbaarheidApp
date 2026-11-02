package nl.ovfietsbeschikbaarheid.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OverviewDataModel

class OverviewRepository(
    private val locationsMapper: LocationsMapper,
    private val httpClient: KtorApiClient,
    private val locationsCacheRepository: LocationsCacheRepository,
) {
    private var lastResult: OverviewDataModel? = null

    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Returns the last cached result.
     * In the off chance there is no cached result (which should only happen if the app ran out of memory),
     * the data is loaded again. This will throw an exception when there is no internet for example.
     */
    suspend fun getCachedOrLoad(): OverviewDataModel {
        lastResult?.let {
            return it
        }
        return getOverviewData()
    }

    suspend fun getOverviewData(): OverviewDataModel {
        val locations = httpClient.getLocations()
        storeScope.launch { locationsCacheRepository.store(locations) }
        val mappedLocations = locationsMapper.map(locations)
        val pricePer24Hours = locationsMapper.getPricePer24Hours(locations)
        val result = OverviewDataModel(
            locations = mappedLocations,
            pricePer24Hours = pricePer24Hours,
        )
        lastResult = result
        return result
    }

    suspend fun getStoredLocationsWithoutRentalBikesAvailable(): List<LocationOverviewModel>? {
        return locationsCacheRepository.getLocations()?.let { locationsMapper.mapWithoutRentalBikesAvailable(it) }
    }

    fun filterLocations(allLocations: List<LocationOverviewModel>, searchTerm: String): List<LocationOverviewModel> {
        return allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
    }
}
