package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.DetailsMapper
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.state.setRefreshing
import timber.log.Timber

private const val MIN_REFRESH_TIME = 350L

class DetailsViewModel(
    private val client: KtorApiClient,
    private val overviewRepository: OverviewRepository,
    private val stationRepository: StationRepository
) : ViewModel() {

    val screenState: State<ScreenState<DetailsModel>>
        field = mutableStateOf<ScreenState<DetailsModel>>(ScreenState.Loading)

    val title: State<String>
        field = mutableStateOf("")

    private lateinit var overviewModel: LocationOverviewModel

    fun setLocationCode(locationCode: String) {
        overviewModel = overviewRepository.getAllLocations().find { it.locationCode == locationCode }!!
        title.value = overviewModel.title
        viewModelScope.launch {
            doRefresh()
        }
    }

    fun onReturnToScreenTriggered() {
        if (screenState.value is ScreenState.Loaded) {
            screenState.setRefreshing()
            viewModelScope.launch {
                doRefresh()
            }
        }
    }

    fun onPullToRefresh() {
        viewModelScope.launch {
            screenState.setRefreshing()
            doRefresh(MIN_REFRESH_TIME)
        }
    }

    fun onRetryClick() {
        screenState.value = ScreenState.Loading
        viewModelScope.launch {
            doRefresh()
        }
    }

    private suspend fun doRefresh(minDelay: Long = 0L) {
        try {
            val before = System.currentTimeMillis()
            val details = client.getDetails(overviewModel.uri)
            val allStations = stationRepository.getAllStations()
            val capabilities = stationRepository.getCapacities()
            val data = DetailsMapper.convert(details, overviewRepository.getAllLocations(), allStations, capabilities)
            val timeElapsed = System.currentTimeMillis() - before
            if (timeElapsed < minDelay) {
                delay(minDelay - timeElapsed)
            }
            screenState.value = ScreenState.Loaded(data)
        } catch (e: Exception) {
            Timber.e(e)
            screenState.value = ScreenState.FullPageError
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}