package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.model.VehicleModel
import nl.ovfietsbeschikbaarheid.repository.VehiclesRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.state.setRefreshing
import timber.log.Timber

class MapViewModel(
    private val vehiclesRepository: VehiclesRepository,
) : ViewModel() {

    private val _screenState = mutableStateOf<ScreenState<List<VehicleModel>>>(ScreenState.Loading)
    val screenState: State<ScreenState<List<VehicleModel>>> = _screenState

    fun screenLaunched() {
        viewModelScope.launch {
            doRefresh()
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
            val details = vehiclesRepository.getAllVehicles()
            _screenState.value = ScreenState.Loaded(details)
        } catch (e: Exception) {
            Timber.e(e)
            _screenState.value = ScreenState.FullPageError
        }
    }
}