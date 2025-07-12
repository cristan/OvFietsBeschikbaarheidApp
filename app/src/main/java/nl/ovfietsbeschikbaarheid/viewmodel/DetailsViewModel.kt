package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.ext.atStartOfDay
import nl.ovfietsbeschikbaarheid.mapper.DetailsMapper
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.state.setRefreshing
import nl.ovfietsbeschikbaarheid.util.dutchZone
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
        doRefresh()
    }

    fun onReturnToScreenTriggered() {
        if (screenState.value is ScreenState.Loaded) {
            _screenState.setRefreshing()
            doRefresh()
        }
    }

    fun onPullToRefresh() {
        _screenState.setRefreshing()
        doRefresh(MIN_REFRESH_TIME)
    }

    fun onRetryClick() {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch {
            doRefresh()
        }
    }

    private fun doRefresh(minDelay: Long = 0L) {
        viewModelScope.launch {
            try {
                val before = System.currentTimeMillis()

                val detailsDeferred = async {
                    client.getDetails(data.uri)
                }
                val allLocationsDeferred = async {
                    // No need to go for the non-cached locations: these are only for the alternatives, and these barely change at all
                    overviewRepository.getCachedOrLoad()
                }
                val allStationsDeferred = async {
                    stationRepository.getAllStations()
                }
                val capacitiesDeferred = async {
                    stationRepository.getCapacities()
                }
                val historyDeferred = async {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
                    val startDate = ZonedDateTime.now(dutchZone).atStartOfDay().withZoneSameInstant(ZoneOffset.UTC).format(formatter)
                    client.getHistory(data.locatonCode, startDate)
                }

                val details = detailsDeferred.await()
                if (details == null) {
                    val fetchTimeInstant = Instant.ofEpochSecond(data.fetchTime)
                    val lastFetched = LocalDateTime.ofInstant(fetchTimeInstant, ZoneId.of("Europe/Amsterdam"))!!
                    _screenState.value = ScreenState.Loaded(DetailsContent.NotFound(data.title, lastFetched))
                    return@launch
                }

                val data = DetailsMapper.convert(details, allLocationsDeferred.await(), allStationsDeferred.await(), capacitiesDeferred.await(), historyDeferred.await())
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