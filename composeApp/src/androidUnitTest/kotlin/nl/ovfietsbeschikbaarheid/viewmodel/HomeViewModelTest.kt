package nl.ovfietsbeschikbaarheid.viewmodel

import dev.jordond.compass.Coordinates
import dev.jordond.compass.permissions.PermissionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.model.OverviewDataModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.testutils.MainDispatcherRule
import nl.ovfietsbeschikbaarheid.testutils.shouldBeEqualTo
import nl.ovfietsbeschikbaarheid.usecase.FindNearbyLocationsUseCase
import nl.ovfietsbeschikbaarheid.util.AndroidLocationLoader
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import nl.ovfietsbeschikbaarheid.util.InAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import nl.ovfietsbeschikbaarheid.util.RatingEligibilityService
import org.junit.Rule
import org.junit.Test
import java.net.UnknownHostException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HomeViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    private val findNearbyLocationsUseCase: FindNearbyLocationsUseCase = mockk()
    private val overviewRepository: OverviewRepository = mockk()
    private val locationPermissionHelper: LocationPermissionHelper = mockk(relaxUnitFun = true)
    private val locationLoader: AndroidLocationLoader = mockk()
    private val locationsMapper = LocationsMapper(DecimalFormatter())
    private val ratingEligibilityService: RatingEligibilityService = mockk(relaxed = true)
    private val inAppReviewProvider: InAppReviewProvider = mockk()
    private val viewModel = HomeViewModel(findNearbyLocationsUseCase, overviewRepository, locationPermissionHelper, locationLoader, locationsMapper, ratingEligibilityService, inAppReviewProvider)

    @Test
    fun `starting up the app when you have all the permissions and everything works - GPS first`() = runTest {
        stubLocationsOk(listOf(TestData.testLocationOverviewModel), delay = 1000L)
        every { locationPermissionHelper.isGpsTurnedOn() } returns true
        every { locationPermissionHelper.hasGpsPermission() } returns true
        coEvery { locationLoader.loadCurrentCoordinates() } coAnswers {
            delay(500L)
            Coordinates(51.46, 6.16)
        }

        viewModel.onScreenLaunched()

        assertSame(viewModel.content.value, HomeContent.Loading)

        advanceTimeBy(700L)
        // The GPS is loaded now, but we're still waiting on the backend
        assertSame(viewModel.content.value, HomeContent.Loading)

        advanceTimeBy(400L)
        // All data is now loaded

        assertIs<HomeContent.GpsContent>(viewModel.content.value)
        val gpsContent = viewModel.content.value as HomeContent.GpsContent
        assertFalse(gpsContent.isRefreshing)

        gpsContent.locations shouldBeEqualTo listOf(LocationOverviewWithDistanceModel("103,1 km", TestData.testLocationOverviewModel))
    }

    @Test
    fun `starting up the app when you have all the permissions and everything works - network first`() = runTest {
        stubLocationsOk(delay = 500L)
        every { locationPermissionHelper.isGpsTurnedOn() } returns true
        every { locationPermissionHelper.hasGpsPermission() } returns true
        coEvery { locationLoader.loadCurrentCoordinates() } coAnswers {
            delay(1000L)
            Coordinates(51.46, 6.16)
        }
        coEvery { locationLoader.getLastKnownCoordinates() }.returns(Coordinates(52.46, 6.16))

        viewModel.onScreenLaunched()

        assertSame(viewModel.content.value, HomeContent.Loading)

        advanceTimeBy(700L)
        // The locations are loaded now from the backend, but we're still waiting on the GPS. The distance based on the last known coordinates is shown.

        val initialViewModelContentValue = viewModel.content.value
        assertIs<HomeContent.GpsContent>(initialViewModelContentValue)
        assertTrue(initialViewModelContentValue.isRefreshing)
        assertEquals(initialViewModelContentValue.locations[0].distance, "23,5 km")

        advanceTimeBy(400L)
        // All data is now loaded

        val finalViewModelContentValue = viewModel.content.value
        assertIs<HomeContent.GpsContent>(finalViewModelContentValue)
        assertFalse(finalViewModelContentValue.isRefreshing)
        assertEquals(finalViewModelContentValue.locations[0].distance, "103,1 km")
    }

    @Test
    fun `searching - typing something else before the reverse geocode completes`() = runTest {
        val utrecht = TestData.testLocationOverviewModel.copy(title = "Utrecht")
        val utrechtTerwijde = TestData.testLocationOverviewModel.copy(title = "Utrecht Terwijde")
        val allLocations = listOf(utrecht, utrechtTerwijde)
        launchWithEverythingOk(allLocations)

        // Searching for the first search term is slow
        val nearbyUtrecht = listOf(
            LocationOverviewWithDistanceModel("0 km", utrecht),
            LocationOverviewWithDistanceModel("10 km", utrechtTerwijde)
        )
        coEvery { findNearbyLocationsUseCase.invoke("utrecht", allLocations) } coAnswers {
            delay(1000L)

            nearbyUtrecht
        }

        // Searching for the second search term is faster
        val nearbyUtrechtTerwijde = listOf(
            LocationOverviewWithDistanceModel("0 km", utrechtTerwijde),
            LocationOverviewWithDistanceModel("10 km", utrecht)
        )
        coEvery { findNearbyLocationsUseCase.invoke("utrecht terwijde", allLocations) } coAnswers {
            delay(500L)

            nearbyUtrechtTerwijde
        }

        // Searching for the first search term immediately shows the search results while we're searching where the search term is
        viewModel.onSearchTermChanged("utrecht")
        viewModel.content.value shouldBeEqualTo HomeContent.SearchTermContent(allLocations, "utrecht", null)

        // Before the data is loaded, the user is searching for something else
        viewModel.onSearchTermChanged("utrecht terwijde")
        viewModel.content.value shouldBeEqualTo HomeContent.SearchTermContent(listOf(utrechtTerwijde), "utrecht", null)

        // At the end, the search results of the lastly entered search term are shown
        advanceUntilIdle()
        viewModel.content.value shouldBeEqualTo HomeContent.SearchTermContent(
            listOf(utrechtTerwijde),
            "utrecht terwijde",
            nearbyUtrechtTerwijde
        )
    }

    @Test
    fun `searching - clearing the search term before reverse geocode could complete`() = runTest {
        val allLocations = listOf(TestData.testLocationOverviewModel)
        launchWithEverythingOk(allLocations)

        val searchTerm = allLocations[0].title
        coEvery { findNearbyLocationsUseCase.invoke(searchTerm, allLocations) } coAnswers {
            delay(1000L)

            listOf(LocationOverviewWithDistanceModel("0 km", TestData.testLocationOverviewModel))
        }

        viewModel.onSearchTermChanged(searchTerm)
        viewModel.content.value shouldBeEqualTo HomeContent.SearchTermContent(allLocations, searchTerm, null)

        viewModel.onSearchTermChanged("")
        assertIs<HomeContent.GpsContent>(viewModel.content.value)

        advanceUntilIdle()
        assertIs<HomeContent.GpsContent>(viewModel.content.value)
    }

    @Test
    fun `GPS turned off, then turned on`() = runTest {
        // Internet works fine and GPS permissions were already granted
        every { locationPermissionHelper.hasGpsPermission() }.returns(true)
        stubLocationsOk()

        // Screen launching with working GPS, but turned off
        coEvery { locationLoader.loadCurrentCoordinates() } returns Coordinates(51.46, 6.16)
        every { locationPermissionHelper.isGpsTurnedOn() }.returns(false)

        viewModel.onScreenLaunched()
        viewModel.content.value shouldBeEqualTo HomeContent.GpsTurnedOff

        // User clicks on turn on GPS
        viewModel.onTurnOnGpsClicked()
        verify { locationPermissionHelper.turnOnGps() }

        // User turned on GPS
        every { locationPermissionHelper.isGpsTurnedOn() }.returns(true)
        viewModel.onReturnedToScreen()

        // No further complications and data is shown
        assertIs<HomeContent.GpsContent>(viewModel.content.value)
    }

    @Test
    fun `user keeps on stubbornly denying GPS permissions until they finally cave`() = runTest {
        // First app boot (no permissions, but with internet)
        stubLocationsOk()
        every { locationPermissionHelper.isGpsTurnedOn() } returns true
        every { locationPermissionHelper.hasGpsPermission() } returns false
        every { locationPermissionHelper.shouldShowLocationRationale() } returns false

        viewModel.onScreenLaunched()

        viewModel.content.value shouldBeEqualTo HomeContent.AskGpsPermission(AskPermissionState.Initial)

        // User clicks on "OV-Fiets locaties in je buurt", but doesn't accept the GPS permission
        coEvery { locationPermissionHelper.requirePermission() } returns PermissionState.Denied
        viewModel.onRequestPermissionsClicked(AskPermissionState.Initial)

        viewModel.content.value shouldBeEqualTo HomeContent.AskGpsPermission(AskPermissionState.Denied)

        // User clicks on the button but denies again
        coEvery { locationPermissionHelper.requirePermission() } returns PermissionState.DeniedForever
        viewModel.onRequestPermissionsClicked(AskPermissionState.Denied)

        viewModel.content.value shouldBeEqualTo HomeContent.AskGpsPermission(AskPermissionState.DeniedPermanently)

        // User clicks on open settings, and finally gives permission
        viewModel.onRequestPermissionsClicked(AskPermissionState.DeniedPermanently)
        verify { locationPermissionHelper.openSettings() }

        every { locationPermissionHelper.hasGpsPermission() } returns true
        coEvery { locationLoader.loadCurrentCoordinates() } returns Coordinates(51.46, 6.16)
        viewModel.onReturnedToScreen()

        assertIs<HomeContent.GpsContent>(viewModel.content.value)
    }

    @Test
    fun `user starts searching before the locations are loaded from the backend`() = runTest {
        stubGpsOk()
        every { overviewRepository.filterLocations(any(), any()) } answers { callOriginal() }

        coEvery { overviewRepository.getOverviewData() } coAnswers {
            delay(1000L)
            OverviewDataModel(listOf(TestData.testLocationOverviewModel), "4,65")
        }

        viewModel.onScreenLaunched()
        viewModel.content.value shouldBeEqualTo HomeContent.Loading

        coEvery { findNearbyLocationsUseCase.invoke(any(), any()) } returns null // We're not testing nearby locations here
        val searchTerm = TestData.testLocationOverviewModel.title
        viewModel.onSearchTermChanged(searchTerm)

        // Should show loading until the locations are loaded
        viewModel.content.value shouldBeEqualTo HomeContent.Loading

        advanceUntilIdle()

        // After the locations are loaded, the results by the current search term should be shown
        viewModel.content.value shouldBeEqualTo HomeContent.SearchTermContent(listOf(TestData.testLocationOverviewModel), searchTerm, null)
    }

    @Test
    fun `locations could not be loaded from the backend which is successfully retried`() = runTest {
        stubGpsOk()
        coEvery { overviewRepository.getOverviewData() } throws UnknownHostException()

        viewModel.onScreenLaunched()

        viewModel.content.value shouldBeEqualTo HomeContent.NetworkError

        stubLocationsOk()

        viewModel.onRetryClicked()

        assertIs<HomeContent.GpsContent>(viewModel.content.value)
    }

    // TODO: you start up the app, GPS is slow, you type something and clear it again
    //  it shouldn't load the GPS again

    @Test
    fun `data gets reloaded when the user types at the network error screen`() = runTest {
        // Start with network error
        stubGpsOk()
        coEvery { overviewRepository.getOverviewData() } throws UnknownHostException()

        viewModel.onScreenLaunched()

        viewModel.content.value shouldBeEqualTo HomeContent.NetworkError

        // Start typing by the time your network is ok again
        stubLocationsOk()
        coEvery { findNearbyLocationsUseCase.invoke(any(), any()) } returns null // We're not testing nearby locations here

        viewModel.onSearchTermChanged("a")
        stubLocationsOk(delay = 500L)

        assertIs<HomeContent.SearchTermContent>(viewModel.content.value)
    }

    @Test
    fun `app doesn't crash when you search at the network error screen and you still don't have internet`() = runTest {
        // Start with network error
        stubGpsOk()
        coEvery { overviewRepository.getOverviewData() } throws UnknownHostException()

        viewModel.onScreenLaunched()

        viewModel.content.value shouldBeEqualTo HomeContent.NetworkError

        viewModel.onSearchTermChanged("a")

        viewModel.content.value shouldBeEqualTo HomeContent.NetworkError
    }

    @Test
    fun `data is refreshed after the user returns after 5+ minutes`() = runTest {
        launchWithEverythingOk()

        assertIs<HomeContent.GpsContent>(viewModel.content.value)

        stubLocationsOk(listOf(TestData.testLocationOverviewModel.copy(rentalBikesAvailable = 1337)))

        viewModel.onReturnedToScreen()

        // Even though we have returned to the screen, the data isn't reloaded because it was right after
        coVerify(exactly = 1) { overviewRepository.getOverviewData() }

        val inFiveAndAHalfMinutes = Clock.System.now()
            .plus(5, DateTimeUnit.MINUTE)
            .plus(30, DateTimeUnit.SECOND)

        viewModel.onReturnedToScreen(inFiveAndAHalfMinutes)

        coVerify(exactly = 2) { overviewRepository.getOverviewData() }

        val viewModelContent = viewModel.content.value
        assertIs<HomeContent.GpsContent>(viewModelContent)
        viewModelContent.locations[0].location.rentalBikesAvailable shouldBeEqualTo 1337
    }

    @Test
    fun `both the GPS locations as well as the data from the backend is refreshed at a pull to refresh`() = runTest {
        stubLocationsOk(
            listOf(
                TestData.testLocationOverviewModel,
                TestData.testLocationOverviewModel.copy(
                    title = "Utrecht Centraal Stationsplein",
                    latitude = 52.090746,
                    longitude = 5.110702,
                )
            )
        )
        stubGpsOk()
        viewModel.onScreenLaunched()

        assertIs<HomeContent.GpsContent>(viewModel.content.value)

        stubGpsOk(Coordinates(latitude = 52.090746, longitude = 5.110702))

        viewModel.onPullToRefresh()

        coVerify(exactly = 2) { overviewRepository.getOverviewData() }
        coVerify(exactly = 2) { locationLoader.loadCurrentCoordinates() }

        val viewModelContent = viewModel.content.value
        assertIs<HomeContent.GpsContent>(viewModelContent)
        viewModelContent.locations[0].location.title shouldBeEqualTo "Utrecht Centraal Stationsplein"
    }

    @Test
    fun `gps isn't refreshed again when going to the GPS list again right away`() = runTest {
        launchWithEverythingOk()

        assertIs<HomeContent.GpsContent>(viewModel.content.value)

        coVerify(exactly = 1) { locationLoader.loadCurrentCoordinates() }

        coEvery { findNearbyLocationsUseCase.invoke(any(), any()) } returns null // We're not testing nearby locations here

        viewModel.onSearchTermChanged("a")
        viewModel.onSearchTermChanged("")

        assertIs<HomeContent.GpsContent>(viewModel.content.value)

        coVerify(exactly = 1) { locationLoader.loadCurrentCoordinates() }
    }

    private fun launchWithEverythingOk(allLocations: List<LocationOverviewModel> = listOf(TestData.testLocationOverviewModel)) {
        stubLocationsOk(allLocations)
        stubGpsOk()
        viewModel.onScreenLaunched()
    }

    private fun stubLocationsOk(
        allLocations: List<LocationOverviewModel> = listOf(TestData.testLocationOverviewModel),
        delay: Long = 0L
    ) {
        coEvery { overviewRepository.getOverviewData() } coAnswers {
            if (delay != 0L) {
                delay(delay)
            }
            OverviewDataModel(allLocations, "4,65")
        }

        // This doesn't call a backend, so the regular method can be called.
        every { overviewRepository.filterLocations(any(), any()) } answers { callOriginal() }
    }

    private fun stubGpsOk(coordinates: Coordinates = Coordinates(51.46, 6.16)) {
        every { locationPermissionHelper.isGpsTurnedOn() } returns true
        every { locationPermissionHelper.hasGpsPermission() } returns true
        coEvery { locationLoader.loadCurrentCoordinates() } returns coordinates
    }
}