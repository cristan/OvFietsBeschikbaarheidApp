package com.ovfietsbeschikbaarheid.repository

import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import kotlinx.coroutines.flow.MutableStateFlow

object OverviewRepository {
    val allLocations = MutableStateFlow<List<LocationOverviewModel>>(emptyList())
}
