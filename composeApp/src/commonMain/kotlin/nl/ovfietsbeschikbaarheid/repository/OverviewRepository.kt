package nl.ovfietsbeschikbaarheid.repository

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.OverviewDataModel

class OverviewRepository(
    private val locationsMapper: LocationsMapper,
    private val httpClient: KtorApiClient
) {
    suspend fun getOverviewData(): OverviewDataModel {
        val locations = httpClient.getLocations()
        val mappedLocations = locationsMapper.map(locations)
        val pricePer24Hours = locationsMapper.getPricePer24Hours(locations)
        return OverviewDataModel(
            locations = mappedLocations,
            pricePer24Hours = pricePer24Hours,
        )
    }

    fun filterLocations(allLocations: List<LocationOverviewModel>, searchTerm: String): List<LocationOverviewModel> {
        return allLocations.filter { it.title.contains(searchTerm, ignoreCase = true) }
    }
}
