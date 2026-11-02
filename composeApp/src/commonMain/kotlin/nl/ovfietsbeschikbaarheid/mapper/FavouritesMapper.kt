package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.model.Favourite
import nl.ovfietsbeschikbaarheid.model.FavouritesModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel

object FavouritesMapper {
    fun matchFavouritesWithLocations(favourites: List<Favourite>, allLocations: List<LocationOverviewModel>): FavouritesModel {
        val favouriteLocationCodes = favourites.map { it.locationCode }.toSet()
        val locations = allLocations.filter { it.locationCode in favouriteLocationCodes }
        val foundLocationCodes = locations.map { it.locationCode }.toSet()
        return FavouritesModel(
            locations = locations,
            favouritesWithoutLocation = favourites.filter { it.locationCode !in foundLocationCodes },
        )
    }
}
