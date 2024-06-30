package com.ovfietsbeschikbaarheid

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ovfietsbeschikbaarheid.mapper.LocationsMapper
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class LocationsViewModel : ViewModel() {

    private val _allLocations = MutableStateFlow<List<LocationOverviewModel>>(emptyList())
    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm

    val filteredLocations = _allLocations.combine(searchTerm) { allLocations, searchTerm ->
        if (searchTerm.isEmpty()) {
            allLocations
        }else{
            allLocations.filter { it.entry.title.contains(searchTerm.trim(), ignoreCase = true) }
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
                _allLocations.value = LocationsMapper.map(response)
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