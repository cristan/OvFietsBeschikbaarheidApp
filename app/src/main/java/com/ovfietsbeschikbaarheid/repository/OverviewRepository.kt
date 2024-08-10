package com.ovfietsbeschikbaarheid.repository

import android.content.Context
import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.mapper.LocationsMapper
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object OverviewRepository {
    private val client = KtorApiClient()

    private val allLocations = MutableStateFlow<List<LocationOverviewModel>>(emptyList())

    // allLocations will be empty after app is recreated. This works around that, but there's probably a nicer way to do this.
    fun getAllLocations(context: Context): StateFlow<List<LocationOverviewModel>> {
        if (allLocations.value.isEmpty()) {
            val response = client.getLocations(context)
            allLocations.value = LocationsMapper.map(response)
        }
        return allLocations
    }
}
