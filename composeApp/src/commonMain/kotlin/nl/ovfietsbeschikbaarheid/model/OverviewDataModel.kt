package nl.ovfietsbeschikbaarheid.model

data class OverviewDataModel(
    val locations: List<LocationOverviewModel>,
    val pricePer24Hours: String?
)
