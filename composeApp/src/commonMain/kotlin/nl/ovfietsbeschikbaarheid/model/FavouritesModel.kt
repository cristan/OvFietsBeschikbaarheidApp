package nl.ovfietsbeschikbaarheid.model

data class FavouritesModel(
    val locations: List<LocationOverviewModel>,
    val favouritesWithoutLocation: List<Favourite>,
)
