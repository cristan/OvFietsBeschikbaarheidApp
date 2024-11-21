package nl.ovfietsbeschikbaarheid.viewmodel

import dev.jordond.compass.Coordinates
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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

class HomeViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    private val findNearbyLocationsUseCase: FindNearbyLocationsUseCase = mockk()
    private val overviewRepository: OverviewRepository = mockk()
    private val locationPermissionHelper: LocationPermissionHelper = mockk()
    private val locationLoader: LocationLoader = mockk()
    private val viewModel = HomeViewModel(findNearbyLocationsUseCase, overviewRepository, locationPermissionHelper, locationLoader)

    @Test
    fun `starting up the app when you have all the permissions and everything works`() = runTest {
        coEvery { overviewRepository.getResult() } returns Result.success(listOf(TestData.testLocationOverviewModel))
        every { locationPermissionHelper.isGpsTurnedOn() } returns true
        every { locationPermissionHelper.hasGpsPermission() } returns true
        coEvery { locationLoader.loadCurrentCoordinates() } returns Coordinates(51.46, 6.16)

        viewModel.onScreenLaunched()

        viewModel.content.value shouldBeInstanceOf HomeContent.GpsContent::class
        val gpsContent = viewModel.content.value as HomeContent.GpsContent
        gpsContent.isRefreshing shouldBe false
        gpsContent.locations shouldBeEqualTo listOf(LocationOverviewWithDistanceModel("103,1 km", TestData.testLocationOverviewModel))
    }
}