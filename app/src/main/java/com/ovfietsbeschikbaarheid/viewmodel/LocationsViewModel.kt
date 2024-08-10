package com.ovfietsbeschikbaarheid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.mapper.LocationsMapper
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class LocationsViewModel(application: Application) : AndroidViewModel(application) {
    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm

    private val allLocationsFlow = OverviewRepository.getAllLocations(application)
    val filteredLocations = allLocationsFlow.combine(searchTerm) { allLocations, searchTerm ->
        if (searchTerm.isEmpty()) {
            allLocations
        }else{
            allLocations.filter { it.title.contains(searchTerm.trim(), ignoreCase = true) }
        }
    }

    fun onSearchTermChanged(searchTerm: String) {
        _searchTerm.value = searchTerm
    }
}