package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.model.LocationType
import nl.ovfietsbeschikbaarheid.model.MapMarker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapMarkerMapperTest {

    private val amsterdamCentraalIJzijdeWest = TestData.testLocationOverviewModel.copy(
        title = "Amsterdam Centraal IJzijde West",
        locationCode = "asd008",
        stationCode = "ASD",
        latitude = 52.380095,
        longitude = 4.898784,
    )
    private val amsterdamCentraalOost = TestData.testLocationOverviewModel.copy(
        title = "Amsterdam Centraal Oost",
        locationCode = "asd002",
        stationCode = "ASD",
        latitude = 52.37785,
        longitude = 4.902062,
    )
    private val amsterdamCentraalStationsplein = TestData.testLocationOverviewModel.copy(
        title = "Amsterdam Centraal Stationsplein",
        locationCode = "asd010",
        stationCode = "ASD",
        latitude = 52.378482,
        longitude = 4.897678,
    )
    private val amsterdamCentraal = listOf(amsterdamCentraalIJzijdeWest, amsterdamCentraalOost, amsterdamCentraalStationsplein)

    private val groningen = TestData.testLocationOverviewModel.copy(
        title = "Groningen",
        locationCode = "gn001",
        stationCode = "GN",
        latitude = 53.21155,
        longitude = 6.5665,
    )
    private val groningenOvEbike = TestData.testLocationOverviewModel.copy(
        title = "Groningen - OV-ebike",
        locationCode = "gn004",
        stationCode = "GN",
        latitude = 53.21155,
        longitude = 6.5666,
        type = LocationType.EBike,
    )

    private val arnhemCentrum = TestData.testLocationOverviewModel.copy(
        title = "Arnhem Centrum",
        locationCode = "ah001",
        stationCode = "AH",
        latitude = 51.984,
        longitude = 5.9013,
    )
    private val arnhemCentrumOvEbike = TestData.testLocationOverviewModel.copy(
        title = "Arnhem Centrum - OV-ebike",
        locationCode = "ah003",
        stationCode = "AH",
        latitude = 51.9834,
        longitude = 5.90139,
        type = LocationType.EBike,
    )

    @Test
    fun `the locations of a station share a marker when a neighbourhood is shown`() {
        val mapMarkers = MapMarkerMapper.getMapMarkers(amsterdamCentraal, zoomLevel = 13)

        assertEquals(listOf(MapMarker(52.380095, 4.898784, amsterdamCentraal)), mapMarkers)
    }

    @Test
    fun `only the locations within the grouping distance of each other share a marker`() {
        val mapMarkers = MapMarkerMapper.getMapMarkers(amsterdamCentraal, zoomLevel = 14)

        assertEquals(
            listOf(
                MapMarker(52.380095, 4.898784, listOf(amsterdamCentraalIJzijdeWest, amsterdamCentraalStationsplein)),
                MapMarker(52.37785, 4.902062, listOf(amsterdamCentraalOost)),
            ),
            mapMarkers
        )
    }

    @Test
    fun `the locations of a station get a marker each when the station is shown`() {
        val mapMarkers = MapMarkerMapper.getMapMarkers(amsterdamCentraal, zoomLevel = 15)

        assertEquals(
            listOf(
                MapMarker(52.380095, 4.898784, listOf(amsterdamCentraalIJzijdeWest)),
                MapMarker(52.37785, 4.902062, listOf(amsterdamCentraalOost)),
                MapMarker(52.378482, 4.897678, listOf(amsterdamCentraalStationsplein)),
            ),
            mapMarkers
        )
    }

    @Test
    fun `locations a few meters apart share a marker at the highest zoom level`() {
        val mapMarkers = MapMarkerMapper.getMapMarkers(listOf(groningen, groningenOvEbike), zoomLevel = 21)

        assertEquals(listOf(MapMarker(53.21155, 6.5665, listOf(groningen, groningenOvEbike))), mapMarkers)
    }

    @Test
    fun `the marker of a station separates when zooming in`() {
        val mapMarker = MapMarker(52.380095, 4.898784, amsterdamCentraal)

        assertTrue(MapMarkerMapper.separatesWhenZoomingIn(mapMarker))
    }

    @Test
    fun `the marker of an OV-ebike location around the corner separates when zooming in`() {
        // 67 meters apart
        val mapMarker = MapMarker(51.984, 5.9013, listOf(arnhemCentrum, arnhemCentrumOvEbike))

        assertTrue(MapMarkerMapper.separatesWhenZoomingIn(mapMarker))
    }

    @Test
    fun `the marker of locations a few meters apart doesn't separate when zooming in`() {
        val mapMarker = MapMarker(53.21155, 6.5665, listOf(groningen, groningenOvEbike))

        assertFalse(MapMarkerMapper.separatesWhenZoomingIn(mapMarker))
    }
}
