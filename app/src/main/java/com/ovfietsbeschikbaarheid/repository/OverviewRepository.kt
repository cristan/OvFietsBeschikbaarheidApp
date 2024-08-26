package com.ovfietsbeschikbaarheid.repository

import android.content.Context
import com.ovfietsbeschikbaarheid.R
import com.ovfietsbeschikbaarheid.dto.LocationsDTO
import com.ovfietsbeschikbaarheid.mapper.LocationsMapper
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import kotlinx.serialization.json.Json

class OverviewRepository(private val context: Context) {
    private var allLocations = listOf<LocationOverviewModel>()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private fun loadLocations(context: Context): LocationsDTO {
        val locationsStream = context.resources.openRawResource(R.raw.locaties)
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
