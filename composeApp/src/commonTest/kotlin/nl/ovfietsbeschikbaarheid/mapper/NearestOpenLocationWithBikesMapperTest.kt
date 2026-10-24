package nl.ovfietsbeschikbaarheid.mapper

import kotlinx.datetime.LocalDateTime
import nl.ovfietsbeschikbaarheid.dto.OpeningHoursDTO
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationType
import nl.ovfietsbeschikbaarheid.model.NearestOpenLocationWithBikesModel
import nl.ovfietsbeschikbaarheid.model.OpenState
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NearestOpenLocationWithBikesMapperTest {

    private val mapper = NearestOpenLocationWithBikesMapper(LocationsMapper(DecimalFormatter()))

    private val fridayAt0601 = LocalDateTime(2026, 10, 23, 6, 1, 25)

    private val deventerBoerhavelaan = LocationOverviewModel(
        title = "Deventer Boerhavelaan",
        rentalBikesAvailable = 0,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Zelfservice%20OV-fiets%20uitgiftepunt-dv003",
        fetchTime = 1792728085,
        locationCode = "dv003",
        stationCode = "BSLC",
        latitude = 52.2685,
        longitude = 6.175444,
        type = LocationType.Regular,
        openingHours = (1..7).map { OpeningHoursDTO(it, "00:00", "24:00", true) }
    )

    private val deventer = LocationOverviewModel(
        title = "Deventer",
        rentalBikesAvailable = 147,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-dv001",
        fetchTime = 1792728085,
        locationCode = "dv001",
        stationCode = "DV",
        latitude = 52.25645,
        longitude = 6.160837,
        type = LocationType.Regular,
        openingHours = listOf(
            OpeningHoursDTO(1, "05:15", "01:15", true),
            OpeningHoursDTO(2, "04:50", "01:15", true),
            OpeningHoursDTO(3, "05:15", "01:15", true),
            OpeningHoursDTO(4, "04:50", "01:15", true),
            OpeningHoursDTO(5, "05:15", "02:00", true),
            OpeningHoursDTO(6, "06:10", "02:00", true),
            OpeningHoursDTO(7, "04:50", "00:55", true),
        )
    )

    private val amsterdamCentraalOost = LocationOverviewModel(
        title = "Amsterdam Centraal Oost",
        rentalBikesAvailable = 48,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-asd002",
        fetchTime = 1792728085,
        locationCode = "asd002",
        stationCode = "ASD",
        latitude = 52.37785,
        longitude = 4.902062,
        type = LocationType.Regular,
        openingHours = listOf(
            OpeningHoursDTO(1, "07:00", "24:00", false),
            OpeningHoursDTO(2, "07:00", "24:00", false),
            OpeningHoursDTO(3, "07:00", "24:00", false),
            OpeningHoursDTO(4, "07:00", "24:00", false),
            OpeningHoursDTO(5, "07:00", "24:00", false),
            OpeningHoursDTO(6, "08:00", "24:00", false),
            OpeningHoursDTO(7, "09:00", "24:00", false),
        )
    )

    private val amsterdamCentraalStationsplein = LocationOverviewModel(
        title = "Amsterdam Centraal Stationsplein",
        rentalBikesAvailable = 228,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-asd010",
        fetchTime = 1792728085,
        locationCode = "asd010",
        stationCode = "ASD",
        latitude = 52.378482,
        longitude = 4.897678,
        type = LocationType.Regular,
        openingHours = (1..7).map { OpeningHoursDTO(it, "00:00", "24:00", true) }
    )

    private val amsterdamCentraalIJzijdeWest = LocationOverviewModel(
        title = "Amsterdam Centraal IJzijde West",
        rentalBikesAvailable = 129,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-asd008",
        fetchTime = 1792728085,
        locationCode = "asd008",
        stationCode = "ASD",
        latitude = 52.380095,
        longitude = 4.898784,
        type = LocationType.Regular,
        openingHours = listOf(
            OpeningHoursDTO(1, "04:50", "01:45", true),
            OpeningHoursDTO(2, "04:50", "01:40", true),
            OpeningHoursDTO(3, "04:50", "01:45", true),
            OpeningHoursDTO(4, "05:10", "01:45", true),
            OpeningHoursDTO(5, "05:10", "02:10", true),
            OpeningHoursDTO(6, "04:50", "02:10", true),
            OpeningHoursDTO(7, "04:50", "01:45", true),
        )
    )

    private val leidenCentraalCentrumzijde = LocationOverviewModel(
        title = "Leiden Centraal, Centrumzijde",
        rentalBikesAvailable = 73,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-ledn002",
        fetchTime = 1792728085,
        locationCode = "ledn002",
        stationCode = "LEDN",
        latitude = 52.16557,
        longitude = 4.48259,
        type = LocationType.Regular,
        openingHours = (1..5).map { OpeningHoursDTO(it, "06:00", "21:00", false) }
    )

    private val leidenCentraalUitgangLumc = LocationOverviewModel(
        title = "Leiden Centraal, Uitgang LUMC",
        rentalBikesAvailable = 291,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-ledn001",
        fetchTime = 1792728085,
        locationCode = "ledn001",
        stationCode = "LEDN",
        latitude = 52.165578,
        longitude = 4.48155,
        type = LocationType.Regular,
        openingHours = (1..7).map { OpeningHoursDTO(it, "00:00", "24:00", true) }
    )

    private val rotterdamPrinsendam = LocationOverviewModel(
        title = "Rotterdam Fietspunt Wilhelminakade Prinsendam",
        rentalBikesAvailable = 0,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Zelfservice%20OV-fiets%20uitgiftepunt-Rtd003",
        fetchTime = 1792728085,
        locationCode = "Rtd003",
        stationCode = "BSLC",
        latitude = 51.907233,
        longitude = 4.490302,
        type = LocationType.Regular,
        openingHours = (1..7).map { OpeningHoursDTO(it, "00:00", "24:00", true) }
    )

    private val rotterdamBlaak = LocationOverviewModel(
        title = "Rotterdam Blaak",
        rentalBikesAvailable = 6,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-rtb001",
        fetchTime = 1792728085,
        locationCode = "rtb001",
        stationCode = "RTB",
        latitude = 51.920017,
        longitude = 4.488638,
        type = LocationType.Regular,
        openingHours = listOf(
            OpeningHoursDTO(1, "07:00", "19:30", false),
            OpeningHoursDTO(2, "07:00", "19:30", false),
            OpeningHoursDTO(3, "07:00", "19:30", false),
            OpeningHoursDTO(4, "07:00", "19:30", false),
            OpeningHoursDTO(5, "07:00", "21:30", false),
            OpeningHoursDTO(6, "07:00", "18:15", false),
            OpeningHoursDTO(7, "10:30", "18:15", false),
        )
    )

    private val utrechtWestraven = LocationOverviewModel(
        title = "Utrecht P+R Westraven",
        rentalBikesAvailable = 0,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Zelfservice%20OV-fiets%20uitgiftepunt-ut007",
        fetchTime = 1792728085,
        locationCode = "ut007",
        stationCode = "BSLC",
        latitude = 52.05745,
        longitude = 5.10406,
        type = LocationType.Regular,
        openingHours = (1..7).map { OpeningHoursDTO(it, "00:00", "23:59", false) }
    )

    private val utrechtPapendorp = LocationOverviewModel(
        title = "Utrecht P+R Papendorp",
        rentalBikesAvailable = 8,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Zelfservice%20OV-fiets%20uitgiftepunt-ut904",
        fetchTime = 1792728085,
        locationCode = "ut904",
        stationCode = "BSLC",
        latitude = 52.07164,
        longitude = 5.07869,
        type = LocationType.Regular,
        openingHours = (1..7).map { OpeningHoursDTO(it, "00:00", "24:00", true) }
    )

    private val driebergenZeistEbike = LocationOverviewModel(
        title = "Driebergen-Zeist - OV-ebike",
        rentalBikesAvailable = 28,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-db003",
        fetchTime = 1792728085,
        locationCode = "db003",
        stationCode = "DB",
        latitude = 52.065057,
        longitude = 5.2599,
        type = LocationType.EBike,
        openingHours = listOf(
            OpeningHoursDTO(1, "05:40", "01:45", true),
            OpeningHoursDTO(2, "05:40", "01:45", true),
            OpeningHoursDTO(3, "05:40", "01:45", true),
            OpeningHoursDTO(4, "05:45", "01:45", true),
            OpeningHoursDTO(5, "05:40", "01:45", true),
            OpeningHoursDTO(6, "06:40", "01:45", true),
            OpeningHoursDTO(7, "07:40", "01:45", true),
        )
    )

    private val driebergenZeist = LocationOverviewModel(
        title = "Driebergen-Zeist",
        rentalBikesAvailable = 153,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-db001",
        fetchTime = 1792728085,
        locationCode = "db001",
        stationCode = "DB",
        latitude = 52.065057,
        longitude = 5.259785,
        type = LocationType.Regular,
        openingHours = listOf(
            OpeningHoursDTO(1, "05:40", "01:45", true),
            OpeningHoursDTO(2, "05:40", "01:45", true),
            OpeningHoursDTO(3, "05:40", "01:45", true),
            OpeningHoursDTO(4, "05:40", "01:45", true),
            OpeningHoursDTO(5, "05:40", "01:45", true),
            OpeningHoursDTO(6, "06:40", "01:45", true),
            OpeningHoursDTO(7, "07:40", "01:45", true),
        )
    )

    private val eindhovenCentraalNoordzijde = LocationOverviewModel(
        title = "Eindhoven Centraal noordzijde",
        rentalBikesAvailable = 0,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-ehv003",
        fetchTime = 1792728085,
        locationCode = "ehv003",
        stationCode = "EHV",
        latitude = 51.443676,
        longitude = 5.480101,
        type = LocationType.Regular,
        openingHours = (1..5).map { OpeningHoursDTO(it, "06:30", "20:00", false) }
    )

    private val eindhovenCentraalZuidzijde = LocationOverviewModel(
        title = "Eindhoven Centraal zuidzijde (centrum)",
        rentalBikesAvailable = 211,
        uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-ehv002",
        fetchTime = 1792728085,
        locationCode = "ehv002",
        stationCode = "EHV",
        latitude = 51.442199,
        longitude = 5.480273,
        type = LocationType.Regular,
        openingHours = listOf(
            OpeningHoursDTO(1, "04:55", "01:40", true),
            OpeningHoursDTO(2, "04:50", "01:45", true),
            OpeningHoursDTO(3, "04:50", "01:45", true),
            OpeningHoursDTO(4, "04:50", "01:45", true),
            OpeningHoursDTO(5, "04:50", "01:45", true),
            OpeningHoursDTO(6, "05:50", "01:45", true),
            OpeningHoursDTO(7, "06:25", "01:30", true),
        )
    )

    @Test
    fun `an empty location gets the nearest open location with OV-fietsen`() {
        val allLocations = listOf(deventerBoerhavelaan, deventer)

        assertEquals(
            NearestOpenLocationWithBikesModel(
                title = "Deventer",
                locationCode = "dv001",
                fetchTime = 1792728085,
                rentalBikesAvailable = 147,
                distance = "1,7 km",
                openState = OpenState.Open("02:00")
            ),
            mapper.getNearestOpenLocationWithBikes(deventerBoerhavelaan, allLocations, fridayAt0601)
        )
    }

    @Test
    fun `a closed location gets the nearest open location with OV-fietsen`() {
        val allLocations = listOf(amsterdamCentraalOost, amsterdamCentraalStationsplein, amsterdamCentraalIJzijdeWest)

        assertEquals(
            NearestOpenLocationWithBikesModel(
                title = "Amsterdam Centraal Stationsplein",
                locationCode = "asd010",
                fetchTime = 1792728085,
                rentalBikesAvailable = 228,
                distance = "306 m",
                openState = OpenState.Open247
            ),
            mapper.getNearestOpenLocationWithBikes(amsterdamCentraalOost, allLocations, fridayAt0601)
        )
    }

    @Test
    fun `a location closing within the hour gets the nearest open location with OV-fietsen`() {
        val allLocations = listOf(leidenCentraalCentrumzijde, leidenCentraalUitgangLumc)
        val thursdayAt2030 = LocalDateTime(2026, 10, 22, 20, 30)

        assertEquals(
            NearestOpenLocationWithBikesModel(
                title = "Leiden Centraal, Uitgang LUMC",
                locationCode = "ledn001",
                fetchTime = 1792728085,
                rentalBikesAvailable = 291,
                distance = "71 m",
                openState = OpenState.Open247
            ),
            mapper.getNearestOpenLocationWithBikes(leidenCentraalCentrumzijde, allLocations, thursdayAt2030)
        )
    }

    @Test
    fun `a closed location is not suggested`() {
        val allLocations = listOf(rotterdamPrinsendam, rotterdamBlaak)

        assertNull(mapper.getNearestOpenLocationWithBikes(rotterdamPrinsendam, allLocations, fridayAt0601))
    }

    @Test
    fun `a location closing within the hour is not suggested`() {
        val allLocations = listOf(eindhovenCentraalNoordzijde, eindhovenCentraalZuidzijde)
        // Zuidzijde's Thursday opening hours run until 01:45 on Friday
        val fridayAt0100 = LocalDateTime(2026, 10, 23, 1, 0)

        assertNull(mapper.getNearestOpenLocationWithBikes(eindhovenCentraalNoordzijde, allLocations, fridayAt0100))
    }

    @Test
    fun `a location further than 2 km away is not suggested`() {
        // Papendorp is 2,3 km away
        val allLocations = listOf(utrechtWestraven, utrechtPapendorp)

        assertNull(mapper.getNearestOpenLocationWithBikes(utrechtWestraven, allLocations, fridayAt0601))
    }

    @Test
    fun `a regular location is not suggested for an OV-ebike location`() {
        val allLocations = listOf(driebergenZeistEbike, driebergenZeist)
        // The OV-ebikes open at 05:45 on Thursdays, the regular OV-fietsen at 05:40
        val thursdayAt0542 = LocalDateTime(2026, 10, 22, 5, 42)

        assertNull(mapper.getNearestOpenLocationWithBikes(driebergenZeistEbike, allLocations, thursdayAt0542))
    }

    @Test
    fun `an open location with OV-fietsen gets no suggestion`() {
        val allLocations = listOf(amsterdamCentraalStationsplein, amsterdamCentraalOost, amsterdamCentraalIJzijdeWest)

        assertNull(mapper.getNearestOpenLocationWithBikes(amsterdamCentraalStationsplein, allLocations, fridayAt0601))
    }

    @Test
    fun `nothing is suggested when the amounts are unknown`() {
        // LocationsMapper sets every amount to null when locations.json is outdated
        val allLocations = listOf(amsterdamCentraalOost, amsterdamCentraalStationsplein, amsterdamCentraalIJzijdeWest)
            .map { it.copy(rentalBikesAvailable = null) }
        val amsterdamCentraalOostWithUnknownAmount = amsterdamCentraalOost.copy(rentalBikesAvailable = null)

        assertNull(mapper.getNearestOpenLocationWithBikes(amsterdamCentraalOostWithUnknownAmount, allLocations, fridayAt0601))
    }
}
