package nl.ovfietsbeschikbaarheid.model

data class DetailsSkeletonModel(
    val disruptions: String?,
    val directions: String?,
    val about: String?,
    val openingHoursInfo: String?,
    val openingHoursDayCount: Int,
    val alternativeCount: Int,
)

val typicalDetailsSkeleton = DetailsSkeletonModel(
    disruptions = null,
    directions = null,
    about = "Je huurt hier een OV-fiets met het nieuwe OV-fietsslot, waarbij je OV-chipkaart de sleutel is. Meer informatie vind je op ov-fiets.nl/slot.",
    openingHoursInfo = null,
    openingHoursDayCount = 7,
    alternativeCount = 0,
)
