package nl.ovfietsbeschikbaarheid.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import nl.ovfietsbeschikbaarheid.mapper.FavouritesMapper
import nl.ovfietsbeschikbaarheid.model.Favourite
import nl.ovfietsbeschikbaarheid.model.FavouritesModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.repository.FavouritesRepository
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.state.setRefreshing
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FavouritesViewModel(
    private val overviewRepository: OverviewRepository,
    private val favouritesRepository: FavouritesRepository,
) : ViewModel() {

    private val favourites = mutableStateOf<List<Favourite>?>(null)
    private val locationsState = mutableStateOf<ScreenState<LoadedLocations>>(ScreenState.Loading)

    val screenState: State<ScreenState<FavouritesContent>> = derivedStateOf {
        val storedFavourites = favourites.value
        val locationsLoadState = locationsState.value
        when {
            storedFavourites == null -> ScreenState.Loading
            storedFavourites.isEmpty() -> ScreenState.Loaded(FavouritesContent.NoFavourites)
            else -> when (locationsLoadState) {
                ScreenState.Loading -> ScreenState.Loading
                ScreenState.FullPageError -> ScreenState.FullPageError
                is ScreenState.Loaded -> ScreenState.Loaded(
                    FavouritesContent.Content(
                        FavouritesMapper.matchFavouritesWithLocations(storedFavourites, locationsLoadState.data.locations)
                    ),
                    locationsLoadState.isRefreshing
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            favouritesRepository.favourites.collect {
                favourites.value = it
            }
        }
        viewModelScope.launch {
            favouritesRepository.favourites.first { it.isNotEmpty() }
            load(Clock.System.now())
        }
    }

    fun refreshIfOutdated(now: Instant = Clock.System.now()) {
        val locationsLoadState = locationsState.value
        if (locationsLoadState is ScreenState.Loaded && (now - locationsLoadState.data.loadTime).inWholeMinutes >= 5) {
            locationsState.setRefreshing()
            load(now)
        }
    }

    fun onPullToRefresh() {
        locationsState.setRefreshing()
        load(Clock.System.now())
    }

    fun onRetryClick() {
        locationsState.value = ScreenState.Loading
        load(Clock.System.now())
    }

    fun onRemoveFavouriteClick(favourite: Favourite) {
        viewModelScope.launch {
            favouritesRepository.setFavourite(favourite, isFavourite = false)
        }
    }

    private fun load(now: Instant) {
        viewModelScope.launch {
            try {
                locationsState.value = ScreenState.Loaded(LoadedLocations(overviewRepository.getOverviewData().locations, now))
            } catch (e: ResponseException) {
                Logger.e(e) { "Error fetching the locations for the favourites" }
                locationsState.value = ScreenState.FullPageError
            } catch (e: IOException) {
                Logger.e(e) { "Error fetching the locations for the favourites" }
                locationsState.value = ScreenState.FullPageError
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private data class LoadedLocations(
    val locations: List<LocationOverviewModel>,
    val loadTime: Instant,
)

sealed class FavouritesContent {
    data object NoFavourites : FavouritesContent()
    data class Content(val favourites: FavouritesModel) : FavouritesContent()
}
