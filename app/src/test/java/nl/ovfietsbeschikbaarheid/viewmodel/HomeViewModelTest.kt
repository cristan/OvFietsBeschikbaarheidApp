package nl.ovfietsbeschikbaarheid.viewmodel

import dev.jordond.compass.Coordinates
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.model.LocationOverviewWithDistanceModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.testutils.MainDispatcherRule
import nl.ovfietsbeschikbaarheid.usecase.FindNearbyLocationsUseCase
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Rule
import org.junit.Test

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

        viewModel.content.value shouldBe HomeContent.Loading

        advanceTimeBy(700L)
        // The GPS is loaded now, but we're still waiting on the backend
        viewModel.content.value shouldBe HomeContent.Loading

        advanceTimeBy(400L)
        // All data is now loaded

        viewModel.content.value shouldBeInstanceOf HomeContent.GpsContent::class
        val gpsContent = viewModel.content.value as HomeContent.GpsContent
        gpsContent.isRefreshing shouldBe false

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

        viewModel.content.value shouldBe HomeContent.Loading

        advanceTimeBy(700L)
        // The locations are loaded now from the backend, but we're still waiting on the GPS
        viewModel.content.value shouldBe HomeContent.Loading

        advanceTimeBy(400L)
        // All data is now loaded

        viewModel.content.value shouldBeInstanceOf HomeContent.GpsContent::class
    }

    @Test
    fun `GPS turned off, then accepted`() = runTest {
        // Internet works fine and GPS permissions were already granted
        every { locationPermissionHelper.hasGpsPermission() }.returns(true)
        coEvery { overviewRepository.getResult() } returns Result.success(listOf(TestData.testLocationOverviewModel))
        coEvery { locationLoader.loadCurrentCoordinates() } returns Coordinates(51.46, 6.16)

        // Screen launching with GPS turned off
        coEvery { overviewRepository.getResult() } returns Result.success(listOf(TestData.testLocationOverviewModel))
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
        viewModel.content.value shouldBeInstanceOf HomeContent.GpsContent::class
    }
}