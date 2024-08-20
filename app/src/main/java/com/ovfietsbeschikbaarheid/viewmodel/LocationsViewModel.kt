package com.ovfietsbeschikbaarheid.viewmodel

import androidx.lifecycle.ViewModel
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class LocationsViewModel(overviewRepository: OverviewRepository) : ViewModel() {
    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm

    private val allLocationsFlow = overviewRepository.getAllLocations()
    val filteredLocations = allLocationsFlow.combine(searchTerm) { allLocations, searchTerm ->
        if (searchTerm.isEmpty()) {
            allLocations
        } else {
            allLocations.filter { it.title.contains(searchTerm.trim(), ignoreCase = true) }
        }
    }

    fun onSearchTermChanged(searchTerm: String) {
        _searchTerm.value = searchTerm
    }
}