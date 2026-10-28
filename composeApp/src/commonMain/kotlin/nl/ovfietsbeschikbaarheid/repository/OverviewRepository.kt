package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OverviewDataModel

class OverviewRepository(
    private val locationsMapper: LocationsMapper,
    private val httpClient: KtorApiClient
) {
    private var lastResult: LoadedOverview? = null

    /**
     * Returns the last cached result.
     * In the off chance there is no cached result (which should only happen if the app ran out of memory),
     * the data is loaded again. This will throw an exception when there is no internet for example.
     */
    suspend fun getCachedOrLoad(): OverviewDataModel {
        lastResult?.let {
            return it.overviewData
        }
        return getOverviewData()
    }

    fun getCachedLocations(): List<LocationDTO>? = lastResult?.locations

    suspend fun getOverviewData(): OverviewDataModel {
        val locations = httpClient.getLocations()
        val mappedLocations = locationsMapper.map(locations)
        val pricePer24Hours = locationsMapper.getPricePer24Hours(locations)
        val result = OverviewDataModel(
            locations = mappedLocations,
            pricePer24Hours = pricePer24Hours,
        )
        lastResult = LoadedOverview(locations, result)
        return result
    }

    fun filterLocations(allLocations: List<LocationOverviewModel>, searchTerm: String): List<LocationOverviewModel> {
        return allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
    }
}

private class LoadedOverview(
    val locations: List<LocationDTO>,
    val overviewData: OverviewDataModel,
)
