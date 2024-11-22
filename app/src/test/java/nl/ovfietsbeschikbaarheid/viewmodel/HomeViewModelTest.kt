package nl.ovfietsbeschikbaarheid.viewmodel

import dev.jordond.compass.Coordinates
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.testutils.MainDispatcherRule
import nl.ovfietsbeschikbaarheid.testutils.shouldBeEqualTo
import nl.ovfietsbeschikbaarheid.usecase.FindNearbyLocationsUseCase
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    private val findNearbyLocationsUseCase: FindNearbyLocationsUseCase = mockk()
    private val overviewRepository: OverviewRepository = mockk()
    private val locationPermissionHelper: LocationPermissionHelper = mockk(relaxUnitFun = true)
    private val locationLoader: LocationLoader = mockk()
    private val viewModel = HomeViewModel(findNearbyLocationsUseCase, overviewRepository, locationPermissionHelper, locationLoader)

    @Test
    fun `starting up the app when you have all the permissions and everything works - GPS first`() = runTest {
        coEvery { overviewRepository.getResult() } coAnswers {
            delay(1000L)
            Result.success(listOf(TestData.testLocationOverviewModel))
        }
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
        coEvery { overviewRepository.getResult() } coAnswers {
            delay(500L)
            Result.success(listOf(TestData.testLocationOverviewModel))
        }
        every { locationPermissionHelper.isGpsTurnedOn() } returns true
        every { locationPermissionHelper.hasGpsPermission() } returns true
        coEvery { locationLoader.loadCurrentCoordinates() } coAnswers {
            delay(1000L)
            Coordinates(51.46, 6.16)
        }

        viewModel.onScreenLaunched()

        assertSame(viewModel.content.value, HomeContent.Loading)

        advanceTimeBy(700L)
        // The locations are loaded now from the backend, but we're still waiting on the GPS
        assertSame(viewModel.content.value, HomeContent.Loading)

        advanceTimeBy(400L)
        // All data is now loaded

        assertIs<HomeContent.GpsContent>(viewModel.content.value)
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
        viewModel.content.value shouldBeEqualTo HomeContent.SearchTermContent(listOf(utrechtTerwijde), "utrecht terwijde", nearbyUtrechtTerwijde)
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
        coEvery { locationLoader.loadCurrentCoordinates() } returns Coordinates(51.46, 6.16)
        stubLocationsOk()

        // Screen launching with GPS turned off
        every { locationPermissionHelper.isGpsTurnedOn() }.returns(false)
        coEvery { locationLoader.loadCurrentCoordinates() } returns Coordinates(51.46, 6.16)

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

    // TODO: user keeps on stubbornly denying GPS permissions
    // TODO: user starts searching before the locations are loaded from the backend
    // TODO: locations could not be loaded from the backend and are then retried
    // TODO: user starts typing when a full page error is shown

    private fun launchWithEverythingOk(allLocations: List<LocationOverviewModel> = listOf(TestData.testLocationOverviewModel)) {
        stubLocationsOk(allLocations)
        stubGpsOk()
        viewModel.onScreenLaunched()
    }

    private fun stubLocationsOk(allLocations: List<LocationOverviewModel> = listOf(TestData.testLocationOverviewModel)) {
        coEvery { overviewRepository.getResult() } returns Result.success(allLocations)

        // This doesn't call a backend, so the regular method can be called.
        every { overviewRepository.getLocations(any(), any()) } answers { callOriginal() }
    }

    private fun stubGpsOk() {
        every { locationPermissionHelper.isGpsTurnedOn() } returns true
        every { locationPermissionHelper.hasGpsPermission() } returns true
        coEvery { locationLoader.loadCurrentCoordinates() } returns Coordinates(51.46, 6.16)
    }
}