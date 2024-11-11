package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.DetailsMapper
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.state.setRefreshing
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private const val MIN_REFRESH_TIME = 350L

class DetailsViewModel(
    private val client: KtorApiClient,
    private val overviewRepository: OverviewRepository,
    private val stationRepository: StationRepository
) : ViewModel() {

    private val _screenState = mutableStateOf<ScreenState<DetailsContent>>(ScreenState.Loading)
    val screenState: State<ScreenState<DetailsContent>> = _screenState

    private lateinit var data: DetailScreenData

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    fun screenLaunched(data: DetailScreenData) {
        this.data = data
        _title.value = data.title
        viewModelScope.launch {
            doRefresh()
        }
    }

    fun onReturnToScreenTriggered() {
        if (screenState.value is ScreenState.Loaded) {
            _screenState.setRefreshing()
            viewModelScope.launch {
                doRefresh()
            }
        }
    }

    fun onPullToRefresh() {
        viewModelScope.launch {
            _screenState.setRefreshing()
            doRefresh(MIN_REFRESH_TIME)
        }
    }

    fun onRetryClick() {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            doRefresh()
        }
    }

    private suspend fun doRefresh(minDelay: Long = 0L) {
        try {
            val before = System.currentTimeMillis()
            val details = client.getDetails(data.uri)
            if (details == null) {
                val fetchTimeInstant = Instant.ofEpochSecond(data.fetchTime)
                val lastFetched = LocalDateTime.ofInstant(fetchTimeInstant, ZoneId.of("Europe/Amsterdam"))!!
                _screenState.value = ScreenState.Loaded(DetailsContent.NotFound(data.title, lastFetched))
                return
            }
            val allStations = stationRepository.getAllStations()
            val capabilities = stationRepository.getCapacities()
            val allLocations = overviewRepository.getCachedOrLoad().locations
            val data = DetailsMapper.convert(details, allLocations, allStations, capabilities)
            val timeElapsed = System.currentTimeMillis() - before
            if (timeElapsed < minDelay) {
                delay(minDelay - timeElapsed)
            }
            _screenState.value = ScreenState.Loaded(DetailsContent.Content(data))
        } catch (e: Exception) {
            Timber.e(e)
            _screenState.value = ScreenState.FullPageError
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}

sealed class DetailsContent {
    data class NotFound(val locationTitle: String, val lastFetched: LocalDateTime) : DetailsContent()
    data class Content(val details: DetailsModel) : DetailsContent()
}