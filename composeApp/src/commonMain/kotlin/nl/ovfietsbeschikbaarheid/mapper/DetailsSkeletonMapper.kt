package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.ext.getAbout
import nl.ovfietsbeschikbaarheid.ext.getDirections
import nl.ovfietsbeschikbaarheid.ext.getDisruptions
import nl.ovfietsbeschikbaarheid.ext.getOpeningHoursInfo
import nl.ovfietsbeschikbaarheid.ext.isAlternativeTo
import nl.ovfietsbeschikbaarheid.model.DetailsSkeletonModel
import nl.ovfietsbeschikbaarheid.model.typicalDetailsSkeleton

object DetailsSkeletonMapper {
    fun getDetailsSkeleton(locationCode: String, cachedOverviewLocations: List<LocationDTO>?): DetailsSkeletonModel {
        val location = cachedOverviewLocations?.find { it.extra.locationCode == locationCode } ?: return typicalDetailsSkeleton
        return DetailsSkeletonModel(
            disruptions = location.getDisruptions(),
            directions = location.getDirections(),
            about = location.getAbout(),
            openingHoursInfo = location.getOpeningHoursInfo(),
            openingHoursDayCount = location.openingHours?.size ?: 0,
            alternativeCount = cachedOverviewLocations.count { location.isAlternativeTo(it.stationCode, it.extra.locationCode) },
        )
    }
}
