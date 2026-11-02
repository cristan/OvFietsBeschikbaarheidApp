package nl.ovfietsbeschikbaarheid.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.model.Favourite
import nl.ovfietsbeschikbaarheid.model.FavouritesModel
import nl.ovfietsbeschikbaarheid.model.OverviewDataModel
import nl.ovfietsbeschikbaarheid.repository.FavouritesRepository
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.testutils.MainDispatcherRule
import nl.ovfietsbeschikbaarheid.testutils.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FavouritesViewModelTest {

    @get:Rule
    val coroutinesTestRule = MainDispatcherRule()

    private val overviewRepository: OverviewRepository = mockk()
    private val favouritesRepository: FavouritesRepository = mockk()

    private val amsterdamCentraalStationsplein = TestData.testLocationOverviewModel.copy(
        title = "Amsterdam Centraal Stationsplein",
        locationCode = "asd010",
        rentalBikesAvailable = 228,
    )

    @Test
    fun `the locations are not loaded when there are no favourites`() {
        every { favouritesRepository.favourites } returns flowOf(emptyList())

        val viewModel = FavouritesViewModel(overviewRepository, favouritesRepository)

        coVerify(exactly = 0) { overviewRepository.getOverviewData() }
        viewModel.screenState.value shouldBeEqualTo ScreenState.Loaded(FavouritesContent.NoFavourites)
    }

    @Test
    fun `the locations are refreshed after the user returns after 5+ minutes`() {
        every { favouritesRepository.favourites } returns flowOf(listOf(Favourite("asd010", "Amsterdam Centraal Stationsplein")))
        coEvery { overviewRepository.getOverviewData() } returns OverviewDataModel(listOf(amsterdamCentraalStationsplein), "4,80")
        val viewModel = FavouritesViewModel(overviewRepository, favouritesRepository)

        coEvery { overviewRepository.getOverviewData() } returns OverviewDataModel(
            listOf(amsterdamCentraalStationsplein.copy(rentalBikesAvailable = 240)),
            "4,80"
        )

        viewModel.refreshIfOutdated()

        viewModel.getShownFavourites().locations[0].rentalBikesAvailable shouldBeEqualTo 228

        val inFiveAndAHalfMinutes = Clock.System.now()
            .plus(5, DateTimeUnit.MINUTE)
            .plus(30, DateTimeUnit.SECOND)

        viewModel.refreshIfOutdated(inFiveAndAHalfMinutes)

        viewModel.getShownFavourites().locations[0].rentalBikesAvailable shouldBeEqualTo 240
    }

    private fun FavouritesViewModel.getShownFavourites(): FavouritesModel {
        val screenState = screenState.value
        assertIs<ScreenState.Loaded<FavouritesContent>>(screenState)
        val content = screenState.data
        assertIs<FavouritesContent.Content>(content)
        return content.favourites
    }
}
