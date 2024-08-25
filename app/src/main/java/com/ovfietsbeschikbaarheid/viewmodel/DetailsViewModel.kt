package com.ovfietsbeschikbaarheid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.mapper.DetailsMapper
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import com.ovfietsbeschikbaarheid.repository.StationRepository
import com.ovfietsbeschikbaarheid.state.ScreenState
import com.ovfietsbeschikbaarheid.state.setRefreshing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val MIN_REFRESH_TIME = 350

class DetailsViewModel(
    private val overviewRepository: OverviewRepository,
    private val stationRepository: StationRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<DetailsModel>>(ScreenState.Loading)
    val screenState: StateFlow<ScreenState<DetailsModel>> = _screenState

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val client = KtorApiClient()

    private lateinit var overviewModel: LocationOverviewModel


    fun setLocationCode(locationCode: String) {
        overviewModel = overviewRepository.getAllLocations().find { it.locationCode == locationCode }!!
        _title.value = overviewModel.title
        viewModelScope.launch {
            doRefresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _screenState.setRefreshing()
            val before = System.currentTimeMillis()
            doRefresh()
            val timeElapsed = System.currentTimeMillis() - before
            if (timeElapsed < MIN_REFRESH_TIME) {
                delay(MIN_REFRESH_TIME - timeElapsed)
            }
        }
    }

    fun onRetryClick() {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            doRefresh()
        }
    }

    private suspend fun doRefresh() {
        try {
            val details = client.getDetails(overviewModel.uri)
            val allStations = stationRepository.getAllStations()
            val data = DetailsMapper.convert(details, overviewRepository.getAllLocations(), allStations)
            _screenState.value = ScreenState.Loaded(data)
        } catch (e: Exception) {
            _screenState.value = ScreenState.FullPageError
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}