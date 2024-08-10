package com.ovfietsbeschikbaarheid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.mapper.DetailsMapper
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import com.ovfietsbeschikbaarheid.repository.StationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val MIN_REFRESH_TIME = 350

class DetailsViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _detailsPayload = MutableStateFlow<DetailsModel?>(null)
    val detailsPayload: StateFlow<DetailsModel?> = _detailsPayload

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val client = KtorApiClient()

    private lateinit var overviewModel: LocationOverviewModel

    private val allLocationsFlow = OverviewRepository.getAllLocations(application)

    fun setLocationCode(locationCode: String) {
        overviewModel = allLocationsFlow.value.find { it.locationCode == locationCode }!!
        _title.value = overviewModel.title
        _isRefreshing.value = true
        refresh()
        _isRefreshing.value = false
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val before = System.currentTimeMillis()
            doRefresh()
            val timeElapsed = System.currentTimeMillis() - before
            if (timeElapsed < MIN_REFRESH_TIME) {
                delay(MIN_REFRESH_TIME - timeElapsed)
            }
            _isRefreshing.value = false
        }
    }

    private suspend fun doRefresh() {
        val details = client.getDetails(overviewModel.uri)
        val allStations = StationRepository.getAllStations(application)
        _detailsPayload.value = DetailsMapper.convert(details, allLocationsFlow.value, allStations)
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}