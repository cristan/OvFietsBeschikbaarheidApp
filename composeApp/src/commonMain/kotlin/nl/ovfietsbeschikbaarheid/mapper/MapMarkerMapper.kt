package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.MapMarker
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

const val WORLD_WIDTH_DP_AT_ZOOM_LEVEL_0 = 256.0
private const val GROUPING_DISTANCE_DP = 40.0

// The locations which are still grouped at this zoom level are only a few meters apart,
// like an OV-ebike location next to an OV-fiets location. These stay grouped when zooming in further.
private const val MAX_GROUPING_ZOOM_LEVEL = 17

object MapMarkerMapper {
    fun getMapMarkers(locations: List<LocationOverviewModel>, zoomLevel: Int): List<MapMarker> {
        val worldWidth = WORLD_WIDTH_DP_AT_ZOOM_LEVEL_0 * 2.0.pow(min(zoomLevel, MAX_GROUPING_ZOOM_LEVEL))
        return groupLocations(locations, worldWidth, emptyList())
    }

    fun separatesWhenZoomingIn(mapMarker: MapMarker): Boolean {
        return getMapMarkers(mapMarker.locations, MAX_GROUPING_ZOOM_LEVEL).size > 1
    }

    private tailrec fun groupLocations(
        ungroupedLocations: List<LocationOverviewModel>,
        worldWidth: Double,
        mapMarkers: List<MapMarker>
    ): List<MapMarker> {
        val firstLocation = ungroupedLocations.firstOrNull() ?: return mapMarkers
        val (grouped, ungrouped) = ungroupedLocations.partition {
            distanceOnMap(firstLocation, it, worldWidth) <= GROUPING_DISTANCE_DP
        }
        val mapMarker = MapMarker(firstLocation.latitude, firstLocation.longitude, grouped)
        return groupLocations(ungrouped, worldWidth, mapMarkers + mapMarker)
    }

    private fun distanceOnMap(location: LocationOverviewModel, otherLocation: LocationOverviewModel, worldWidth: Double): Double {
        val deltaX = location.webMercatorX() - otherLocation.webMercatorX()
        val deltaY = location.webMercatorY() - otherLocation.webMercatorY()
        return hypot(deltaX, deltaY) * worldWidth
    }

    private fun LocationOverviewModel.webMercatorX() = (longitude + 180) / 360

    private fun LocationOverviewModel.webMercatorY(): Double {
        val sinLatitude = sin(latitude * PI / 180)
        return 0.5 - ln((1 + sinLatitude) / (1 - sinLatitude)) / (4 * PI)
    }
}
