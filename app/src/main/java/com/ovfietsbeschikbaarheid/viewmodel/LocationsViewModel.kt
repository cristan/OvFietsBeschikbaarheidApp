package com.ovfietsbeschikbaarheid.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.mapper.LocationsMapper
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class LocationsViewModel : ViewModel() {

    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm

    val filteredLocations = OverviewRepository.allLocations.combine(searchTerm) { allLocations, searchTerm ->
        if (searchTerm.isEmpty()) {
            allLocations
        }else{
            allLocations.filter { it.title.contains(searchTerm.trim(), ignoreCase = true) }
        }
    }


    private val client = KtorApiClient()

    init {
        fetchLocations()
    }

    private fun fetchLocations() {
        viewModelScope.launch {
            try {
                val response = client.getLocations()
                OverviewRepository.allLocations.value = LocationsMapper.map(response)
            } catch (e: Exception) {
                // Handle the exception (e.g., log it or show an error message)
                e.printStackTrace()
            }
        }
    }

    fun onSearchTermChanged(searchTerm: String) {
        _searchTerm.value = searchTerm
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}