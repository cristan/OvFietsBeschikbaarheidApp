package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.VehicleModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.VehiclesRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import timber.log.Timber

class MapViewModel(
    private val vehiclesRepository: VehiclesRepository,
    private val overviewRepository: OverviewRepository
) : ViewModel() {

    private val _screenState = mutableStateOf<ScreenState<MapContent>>(ScreenState.Loading)
    val screenState: State<ScreenState<MapContent>> = _screenState

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
            val overviewModels = overviewRepository.getCachedOrLoad()
            _screenState.value = ScreenState.Loaded(MapContent(details, overviewModels))
        } catch (e: Exception) {
            Timber.e(e)
            _screenState.value = ScreenState.FullPageError
        }
    }
}

data class MapContent(
    val vehicles: List<VehicleModel>,
    val overviewModels: List<LocationOverviewModel>
)