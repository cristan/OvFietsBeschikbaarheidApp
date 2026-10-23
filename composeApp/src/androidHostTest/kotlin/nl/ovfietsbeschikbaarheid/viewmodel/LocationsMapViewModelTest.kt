package nl.ovfietsbeschikbaarheid.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.model.OverviewDataModel
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.testutils.MainDispatcherRule
import nl.ovfietsbeschikbaarheid.testutils.shouldBeEqualTo
import nl.ovfietsbeschikbaarheid.util.AndroidLocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class LocationsMapViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    private val overviewRepository: OverviewRepository = mockk()
    private val locationPermissionHelper: LocationPermissionHelper = mockk()
    private val locationLoader: AndroidLocationLoader = mockk()

    @Test
    fun `the locations are refreshed after the user returns after 5+ minutes`() {
        coEvery { overviewRepository.getCachedOrLoad() } returns OverviewDataModel(listOf(TestData.testLocationOverviewModel), "4,65")
        every { locationPermissionHelper.hasGpsPermission() } returns false
        val viewModel = LocationsMapViewModel(overviewRepository, locationPermissionHelper, locationLoader)

        coEvery { overviewRepository.getOverviewData() } returns OverviewDataModel(
            listOf(TestData.testLocationOverviewModel.copy(rentalBikesAvailable = 1337)),
            "4,65"
        )

        viewModel.onReturnedToScreen()

        coVerify(exactly = 0) { overviewRepository.getOverviewData() }

        val inFiveAndAHalfMinutes = Clock.System.now()
            .plus(5, DateTimeUnit.MINUTE)
            .plus(30, DateTimeUnit.SECOND)

        viewModel.onReturnedToScreen(inFiveAndAHalfMinutes)

        val screenState = viewModel.screenState.value
        assertIs<ScreenState.Loaded<LocationsMapContent>>(screenState)
        screenState.data.locations[0].rentalBikesAvailable shouldBeEqualTo 1337
    }
}
