package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.dto.OpeningHoursDTO
import nl.ovfietsbeschikbaarheid.model.OpenState
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month
import kotlin.test.assertEquals

class OpenStateMapperTest {

    @Test
    fun `return 247 when open all day`() {
        val openingHours = (1..7).map {
            OpeningHoursDTO(it, "00:00", "24:00", true)
        }
        assertEquals(OpenState.Open247, OpenStateMapper.getOpenState(openingHours, LocalDateTime.now()))
    }

    @Test
    fun `return 247 when open from 00-00 to 00-01`() {
        // Hilversum Sportpark and Amsterdam Lelyplein. Little weird, but ok
        val openingHours = (1..7).map {
            OpeningHoursDTO(it, "00:00", "00:01", false)
        }
        assertEquals(OpenState.Open247, OpenStateMapper.getOpenState(openingHours, LocalDateTime.now()))
    }

    @Test
    fun `return 247 when open from 00-00 to 23-59`() {
        // Quite a few, like Utrecht P+R Westraven
        val openingHours = (1..7).map {
            OpeningHoursDTO(it, "00:00", "23:59", false)
        }
        assertEquals(OpenState.Open247, OpenStateMapper.getOpenState(openingHours, LocalDateTime.now()))
    }

    @Test
    fun `return open when open today`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHoursDTO(1, "06:00", "20:15", false),
            OpeningHoursDTO(2, "06:00", "20:15", false),
            OpeningHoursDTO(3, "06:00", "20:15", false),
            OpeningHoursDTO(4, "06:00", "20:15", false),
            OpeningHoursDTO(5, "06:00", "21:45", false),
        )

        // The first week on 2024 is an easy one: it starts on a monday
        val fridayAt18Past20 = LocalDateTime.of(2024, Month.JULY, 5, 20, 18)

        assertEquals(OpenState.Open("21:45"), OpenStateMapper.getOpenState(openingHours, fridayAt18Past20))
    }

    @Test
    fun `return closed when closed today`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHoursDTO(1, "06:00", "20:15", false),
            OpeningHoursDTO(2, "06:00", "20:15", false),
            OpeningHoursDTO(3, "06:00", "20:15", false),
            OpeningHoursDTO(4, "06:00", "20:15", false),
            OpeningHoursDTO(5, "06:00", "21:45", false),
        )

        val thursdayAt18Past20 = LocalDateTime.of(2024, Month.JULY, 4, 20, 18)

        assertEquals(OpenState.Closed(R.string.day_tomorrow, "06:00"), OpenStateMapper.getOpenState(openingHours, thursdayAt18Past20))
    }

    @Test
    fun `return closed when closed for a few days`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHoursDTO(1, "06:00", "20:15", false),
            OpeningHoursDTO(2, "06:00", "20:15", false),
            OpeningHoursDTO(3, "06:00", "20:15", false),
            OpeningHoursDTO(4, "06:00", "20:15", false),
            OpeningHoursDTO(5, "06:00", "21:45", false),
        )

        val saturdayAt5oClock = LocalDateTime.of(2024, Month.JULY, 6, 5, 0)
        assertEquals(OpenState.Closed(R.string.day_1, "06:00"), OpenStateMapper.getOpenState(openingHours, saturdayAt5oClock))

        val fridayAfterClosing = LocalDateTime.of(2024, Month.JULY, 5, 22, 0)
        assertEquals(OpenState.Closed(R.string.day_1, "06:00"), OpenStateMapper.getOpenState(openingHours, fridayAfterClosing))
    }

    @Test
    fun `handle almost 247`() {
        // P + R Utrecht Science Park (De Uithof)
        // This one is open 24/7 except for 1 day. That's probably incorrect, but the app should still be able to handle it
        val openingHours = listOf(
            OpeningHoursDTO(1, "00:00", "24:00", true),
            OpeningHoursDTO(2, "00:00", "24:00", true),
            OpeningHoursDTO(3, "00:00", "24:00", false),
            OpeningHoursDTO(4, "07:00", "24:00", true),
            OpeningHoursDTO(5, "00:00", "24:00", true),
            OpeningHoursDTO(6, "00:00", "24:00", true),
            OpeningHoursDTO(7, "00:00", "24:00", true),
        )

        val wednesdayDuringTheDay = LocalDateTime.of(2024, Month.JULY, 3, 14, 0)
        assertEquals(OpenState.Open("24:00"), OpenStateMapper.getOpenState(openingHours, wednesdayDuringTheDay))

        // A case which isn't great yet, is a day like monday here. The app will say the station will be open until 24:00,
        // but it will be open until day 4 at 07:00 hours.
        // This is the only place where something like this happens, so I've decided I don't care.
    }

    @Test
    fun `opens this second`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHoursDTO(1, "06:00", "20:15", false),
            OpeningHoursDTO(2, "06:00", "20:15", false),
            OpeningHoursDTO(3, "06:00", "20:15", false),
            OpeningHoursDTO(4, "06:00", "20:15", false),
            OpeningHoursDTO(5, "06:00", "21:45", false),
        )

        val fridayAt6oClock = LocalDateTime.of(2024, Month.JULY, 5, 6, 0)

        assertEquals(OpenState.Open("21:45"), OpenStateMapper.getOpenState(openingHours, fridayAt6oClock))
    }

    @Test
    fun `take into account closesNextDay`() {
        // Zoetermeer Centrum West: a combination of closesNextDay true and false
        val openingHours = listOf(
            OpeningHoursDTO(1, "05:00", "24:00", false),
            OpeningHoursDTO(2, "05:00", "24:00", false),
            OpeningHoursDTO(3, "05:00", "24:00", false),
            OpeningHoursDTO(4, "05:00", "24:00", false),
            OpeningHoursDTO(5, "05:00", "01:00", true),
            OpeningHoursDTO(6, "06:30", "01:00", true),
            OpeningHoursDTO(7, "08:00", "24:00", false),
        )

        val friday0030 = LocalDateTime.of(2024, Month.JULY, 5, 0, 30)
        assertEquals(OpenState.Closed(null, "05:00"), OpenStateMapper.getOpenState(openingHours, friday0030))

        val saturdayAt1030 = LocalDateTime.of(2024, Month.JULY, 6, 1, 30)
        assertEquals(OpenState.Closed(null, "06:30"), OpenStateMapper.getOpenState(openingHours, saturdayAt1030))

        val saturdayAt0030 = LocalDateTime.of(2024, Month.JULY, 6, 0, 30)
        assertEquals(OpenState.Closing("01:00"), OpenStateMapper.getOpenState(openingHours, saturdayAt0030))
    }

    @Test
    fun `don't bug out at 30 seconds before midnight`() {
        // Zoetermeer Driemanspolder
        val openingHours = listOf(
            OpeningHoursDTO(1, "05:00", "24:00", false),
            OpeningHoursDTO(2, "05:00", "24:00", false),
            OpeningHoursDTO(3, "05:00", "24:00", false),
            OpeningHoursDTO(4, "05:00", "24:00", false),
            OpeningHoursDTO(5, "05:00", "01:00", true),
            OpeningHoursDTO(6, "07:00", "01:00", true),
            OpeningHoursDTO(7, "08:30", "24:00", false),
        )


        val friday2359 = LocalDateTime.of(2024, Month.JULY, 5, 23, 58, 30)
        assertEquals(OpenState.Open("01:00"), OpenStateMapper.getOpenState(openingHours, friday2359))

        val thursday2359 = LocalDateTime.of(2024, Month.JULY, 4, 23, 59, 30)
        assertEquals(OpenState.Closing("24:00"), OpenStateMapper.getOpenState(openingHours, thursday2359))

        val sunday2359 = LocalDateTime.of(2024, Month.JULY, 7, 23, 59, 30)
        assertEquals(OpenState.Closing("24:00"), OpenStateMapper.getOpenState(openingHours, sunday2359))
    }
}