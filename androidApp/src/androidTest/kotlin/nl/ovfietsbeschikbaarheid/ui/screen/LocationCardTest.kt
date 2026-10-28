package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import nl.ovfietsbeschikbaarheid.dto.OpeningHoursDTO
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.model.LocationType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class LocationCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun locationClosingSoonIsReadAsOneClickableSentence() {
        val amsterdamNoord = LocationOverviewModel(
            title = "Amsterdam Noord",
            rentalBikesAvailable = 1,
            uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-asd009",
            fetchTime = 1792794852,
            locationCode = "asd009",
            stationCode = "BSLC",
            latitude = 52.402374,
            longitude = 4.931019,
            type = LocationType.Regular,
            openingHours = listOf(
                OpeningHoursDTO(dayOfWeek = 1, startTime = "05:15", endTime = "01:25", closesNextDay = true),
                OpeningHoursDTO(dayOfWeek = 2, startTime = "05:15", endTime = "01:25", closesNextDay = true),
                OpeningHoursDTO(dayOfWeek = 3, startTime = "05:15", endTime = "01:25", closesNextDay = true),
                OpeningHoursDTO(dayOfWeek = 4, startTime = "05:15", endTime = "01:25", closesNextDay = true),
                OpeningHoursDTO(dayOfWeek = 5, startTime = "05:15", endTime = "01:25", closesNextDay = true),
                OpeningHoursDTO(dayOfWeek = 6, startTime = "06:15", endTime = "01:25", closesNextDay = true),
                OpeningHoursDTO(dayOfWeek = 7, startTime = "07:15", endTime = "01:25", closesNextDay = true),
            )
        )

        composeTestRule.setContent {
            LocationCard(amsterdamNoord, "3,3 km", now = Instant.parse("2026-10-24T00:36:00+02:00")) {}
        }

        composeTestRule
            .onNodeWithContentDescription("Amsterdam Noord, 3,3 km, 1 OV-fiets beschikbaar, Sluit snel, Sluit om 01:25")
            .assertHasClickAction()
    }

    @Test
    fun closedLocationIsReadAsOneClickableSentence() {
        val amsterdamCentraalOost = LocationOverviewModel(
            title = "Amsterdam Centraal Oost",
            rentalBikesAvailable = 15,
            uri = "https://places.ns-mlab.nl/api/v2/places/stationfacility/Bemenst%20OV-fiets%20uitgiftepunt-asd002",
            fetchTime = 1792794852,
            locationCode = "asd002",
            stationCode = "ASD",
            latitude = 52.37785,
            longitude = 4.902062,
            type = LocationType.Regular,
            openingHours = listOf(
                OpeningHoursDTO(dayOfWeek = 1, startTime = "07:00", endTime = "24:00", closesNextDay = false),
                OpeningHoursDTO(dayOfWeek = 2, startTime = "07:00", endTime = "24:00", closesNextDay = false),
                OpeningHoursDTO(dayOfWeek = 3, startTime = "07:00", endTime = "24:00", closesNextDay = false),
                OpeningHoursDTO(dayOfWeek = 4, startTime = "07:00", endTime = "24:00", closesNextDay = false),
                OpeningHoursDTO(dayOfWeek = 5, startTime = "07:00", endTime = "24:00", closesNextDay = false),
                OpeningHoursDTO(dayOfWeek = 6, startTime = "08:00", endTime = "24:00", closesNextDay = false),
                OpeningHoursDTO(dayOfWeek = 7, startTime = "09:00", endTime = "24:00", closesNextDay = false),
            )
        )

        composeTestRule.setContent {
            LocationCard(amsterdamCentraalOost, "184 m", now = Instant.parse("2026-10-24T00:36:00+02:00")) {}
        }

        composeTestRule
            .onNodeWithContentDescription("Amsterdam Centraal Oost, 184 m, 15 OV-fietsen beschikbaar, Gesloten, Gaat open om 08:00")
            .assertHasClickAction()
    }
}
