package com.ovfietsbeschikbaarheid.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.mapper.DetailsMapper
import com.ovfietsbeschikbaarheid.model.DetailsModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DetailsViewModel : ViewModel() {

    private val _detailsPayload = MutableStateFlow<DetailsModel?>(null)
    val detailsPayload: StateFlow<DetailsModel?> = _detailsPayload

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val client = KtorApiClient()

    private lateinit var overviewModel: LocationOverviewModel

    fun setLocationCode(locationCode: String) {
        overviewModel = OverviewRepository.allLocations.value.find { it.locationCode == locationCode }!!
        _title.value = overviewModel.title
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val details = client.getDetails(overviewModel.uri)
            _detailsPayload.value = DetailsMapper.convert(details)
            _isRefreshing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}