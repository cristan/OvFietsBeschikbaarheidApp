package nl.ovfietsbeschikbaarheid.viewmodel

import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import java.time.Instant

sealed class HomeContent {
    // Initial empty state while we set things up.
    data object InitialEmpty : HomeContent()

    data class AskGpsPermission(val state: AskPermissionState) : HomeContent()

    data object Loading : HomeContent()

    data object NetworkError : HomeContent()

    data object GpsTurnedOff : HomeContent()

    data object NoGpsLocation : HomeContent()

    data class GpsContent(
        val locations: List<LocationOverviewWithDistanceModel>,
        val fetchTime: Instant,
        val isRefreshing: Boolean = false
    ) : HomeContent()

    data class SearchTermContent(
        val locations: List<LocationOverviewModel>,
        val searchTerm: String,
        val nearbyLocations: List<LocationOverviewWithDistanceModel>?
    ) : HomeContent()

    data class NoSearchResults(val searchTerm: String) : HomeContent()
}

enum class AskPermissionState {
    Initial,
    Denied,
    DeniedPermanently
}