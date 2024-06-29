package com.ovfietsbeschikbaarheid

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ovfietsbeschikbaarheid.dto.DetailsPayload
import com.ovfietsbeschikbaarheid.mapper.DetailsMapper
import com.ovfietsbeschikbaarheid.model.DetailsModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DetailsViewModel : ViewModel() {

    private val _detailsPayload = MutableStateFlow<DetailsModel?>(null)
    val detailsPayload: StateFlow<DetailsModel?> = _detailsPayload

    private val client = KtorApiClient()

    fun setDetailUrl(detailUri: String) {
        viewModelScope.launch {
            val details = client.getDetails(detailUri)
            _detailsPayload.value = DetailsMapper.convert(details)
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}