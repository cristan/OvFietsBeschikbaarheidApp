package nl.ovfietsbeschikbaarheid.repository

import android.content.Context
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.dto.LocationsDTO
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel

class OverviewRepository(private val context: Context) {
    private var allLocations = listOf<LocationOverviewModel>()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun loadLocations(context: Context): LocationsDTO {
        val locationsStream = context.resources.openRawResource(R.raw.locations)
        val inputAsString = locationsStream.bufferedReader().use { it.readText() }
        return json.decodeFromString<LocationsDTO>(inputAsString)
//        return httpClient.get("http://fiets.openov.nl/locaties.json").body<LocationsDTO>()
    }

    fun getAllLocations(): List<LocationOverviewModel> {
        if (allLocations.isEmpty()) {
            val response = loadLocations(context)
            allLocations = LocationsMapper.map(response)
        }
        return allLocations
    }
}
