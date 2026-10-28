package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.model.Favourite
import nl.ovfietsbeschikbaarheid.model.FavouritesModel
import kotlin.test.Test
import kotlin.test.assertEquals

class FavouritesMapperTest {

    private val amsterdamCentraalStationsplein = TestData.testLocationOverviewModel.copy(
        title = "Amsterdam Centraal Stationsplein",
        locationCode = "asd010",
    )
    private val utrechtCentraalStationsplein = TestData.testLocationOverviewModel.copy(
        title = "Utrecht Centraal Stationsplein",
        locationCode = "ut020",
    )
    private val utrechtOvervecht = TestData.testLocationOverviewModel.copy(
        title = "Utrecht Overvecht",
        locationCode = "uto002",
    )
    private val allLocations = listOf(amsterdamCentraalStationsplein, utrechtCentraalStationsplein, utrechtOvervecht)

    @Test
    fun `favourites keep the order of the locations instead of the order they were added`() {
        val favourites = listOf(
            Favourite("ut020", "Utrecht Centraal Stationsplein"),
            Favourite("asd010", "Amsterdam Centraal Stationsplein"),
        )

        val favouritesModel = FavouritesMapper.matchFavouritesWithLocations(favourites, allLocations)

        assertEquals(
            FavouritesModel(
                locations = listOf(amsterdamCentraalStationsplein, utrechtCentraalStationsplein),
                favouritesWithoutLocation = emptyList(),
            ),
            favouritesModel
        )
    }

    @Test
    fun `a favourite whose location code is no longer in the locations is kept without a location`() {
        // Utrecht Overvecht had location code uto001 in 2024
        val favourites = listOf(
            Favourite("uto001", "Utrecht Overvecht"),
            Favourite("asd010", "Amsterdam Centraal Stationsplein"),
        )

        val favouritesModel = FavouritesMapper.matchFavouritesWithLocations(favourites, allLocations)

        assertEquals(
            FavouritesModel(
                locations = listOf(amsterdamCentraalStationsplein),
                favouritesWithoutLocation = listOf(Favourite("uto001", "Utrecht Overvecht")),
            ),
            favouritesModel
        )
    }
}
