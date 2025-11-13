package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.IOException
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.ext.atStartOfDay
import nl.ovfietsbeschikbaarheid.ext.dutchTimeZone
import nl.ovfietsbeschikbaarheid.mapper.DetailsMapper
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.model.DetailsModel
import nl.ovfietsbeschikbaarheid.repository.DetailsRepository
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.state.setRefreshing
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val MIN_REFRESH_TIME = 350L

@OptIn(ExperimentalTime::class)
class DetailsViewModel(
    private val client: KtorApiClient,
    private val detailsMapper: DetailsMapper,
    private val overviewRepository: OverviewRepository,
    private val stationRepository: StationRepository,
    private val detailsRepository: DetailsRepository
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
            supervisorScope {
                try {
                    val before = Clock.System.now().toEpochMilliseconds()

                    val detailsDeferred = async {
                        detailsRepository.getDetails(data.locationCode)
                    }
                    val allLocationsDeferred = async {
                        // No need to go for the non-cached locations: these are only for the alternatives, and these barely change at all
                        overviewRepository.getCachedOrLoad()
                    }
                    val allStationsDeferred = async {
                        stationRepository.getAllStations()
                    }
                    val historyDeferred = async {
                        val customFormat = LocalDateTime.Format {
                            date(LocalDate.Formats.ISO)
                            char('T')
                            time(LocalTime.Formats.ISO)
                            char('Z')
                        }

                        val startDate = Clock.System.now()
                            .minus(7, DateTimeUnit.DAY, dutchTimeZone)
                            .toLocalDateTime(dutchTimeZone).atStartOfDay()
                            .toInstant(dutchTimeZone).toLocalDateTime(TimeZone.UTC)
                            .format(customFormat)

                        client.getHistory(data.locationCode, startDate)
                    }

                    val details = detailsDeferred.await()
                    if (details == null) {
                        val fetchTimeInstant = Instant.fromEpochSeconds(data.fetchTime)

                        // Not perfect. Before, we formatted this to dd MMMM yyyy which looks nicer to Dutch people's eyes.
                        // Not a big problem though: this screen being shown should be exceedingly rare
                        val formattedDate = fetchTimeInstant.toLocalDateTime(dutchTimeZone).date.toString()
                        _screenState.value = ScreenState.Loaded(DetailsContent.NotFound(data.title, formattedDate))
                        return@supervisorScope
                    }

                    val data = detailsMapper.convert(
                        details,
                        allLocationsDeferred.await().locations,
                        allStationsDeferred.await(),
                        historyDeferred.await()
                    )
                    val timeElapsed = Clock.System.now().toEpochMilliseconds() - before
                    if (timeElapsed < minDelay) {
                        delay(minDelay - timeElapsed)
                    }
                    _screenState.value = ScreenState.Loaded(DetailsContent.Content(data))
                } catch (e: ResponseException) {
                    Logger.e(e) { "Error fetching details" }
                    _screenState.value = ScreenState.FullPageError
                } catch (e: IOException) {
                    Logger.e(e) { "Error fetching details" }
                    _screenState.value = ScreenState.FullPageError
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        client.close()
    }
}

sealed class DetailsContent {
    data class NotFound(val locationTitle: String, val formattedLastFetchedDate: String) : DetailsContent()
    data class Content(val details: DetailsModel) : DetailsContent()
}