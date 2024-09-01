package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.dto.OpeningHours
import nl.ovfietsbeschikbaarheid.model.OpenState
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

class OpenStateMapperTest {

    @Test
    fun `return 247 when open all day`() {
        val openingHours = (1..7).map {
            OpeningHours(it, "00:00", "24:00", true)
        }
        OpenStateMapper.getOpenState(openingHours, LocalDateTime.now()) shouldBeEqualTo OpenState.Open247
    }

    @Test
    fun `return 247 when open from 00-00 to 00-01`() {
        // Hilversum Sportpark and Amsterdam Lelyplein. Little weird, but ok
        val openingHours = (1..7).map {
            OpeningHours(it, "00:00", "00:01", false)
        }
        OpenStateMapper.getOpenState(openingHours, LocalDateTime.now()) shouldBeEqualTo OpenState.Open247
    }

    @Test
    fun `return 247 when open from 00-00 to 23-59`() {
        // Quite a few, llike Utrecht P+R Westraven
        val openingHours = (1..7).map {
            OpeningHours(it, "00:00", "23:59", false)
        }
        OpenStateMapper.getOpenState(openingHours, LocalDateTime.now()) shouldBeEqualTo OpenState.Open247
    }

    @Test
    fun `return open when open today`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHours(1, "06:00", "20:15", false),
            OpeningHours(2, "06:00", "20:15", false),
            OpeningHours(3, "06:00", "20:15", false),
            OpeningHours(4, "06:00", "20:15", false),
            OpeningHours(5, "06:00", "21:45", false),
        )

        // The first week on 2024 is an easy one: it starts on a monday
        val fridayAt18Past20 = LocalDateTime.of(2024, Month.JULY, 5, 20, 18)

        OpenStateMapper.getOpenState(openingHours, fridayAt18Past20) shouldBeEqualTo OpenState.Open("21:45")
    }

    @Test
    fun `return closed when closed today`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHours(1, "06:00", "20:15", false),
            OpeningHours(2, "06:00", "20:15", false),
            OpeningHours(3, "06:00", "20:15", false),
            OpeningHours(4, "06:00", "20:15", false),
            OpeningHours(5, "06:00", "21:45", false),
        )

        val thursdayAt18Past20 = LocalDateTime.of(2024, Month.JULY, 4, 20, 18)

        OpenStateMapper.getOpenState(openingHours, thursdayAt18Past20) shouldBeEqualTo OpenState.Closed("morgen", "06:00")
    }

    @Test
    fun `return closed when closed for a few days`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHours(1, "06:00", "20:15", false),
            OpeningHours(2, "06:00", "20:15", false),
            OpeningHours(3, "06:00", "20:15", false),
            OpeningHours(4, "06:00", "20:15", false),
            OpeningHours(5, "06:00", "21:45", false),
        )

        val saturdayAt5oClock = LocalDateTime.of(2024, Month.JULY, 6, 5, 0)

        OpenStateMapper.getOpenState(openingHours, saturdayAt5oClock) shouldBeEqualTo OpenState.Closed("maandag", "06:00")
    }

    @Test
    fun `opens this second`() {
        // Best: open only 5 days a week and longer open on Friday
        val openingHours = listOf(
            OpeningHours(1, "06:00", "20:15", false),
            OpeningHours(2, "06:00", "20:15", false),
            OpeningHours(3, "06:00", "20:15", false),
            OpeningHours(4, "06:00", "20:15", false),
            OpeningHours(5, "06:00", "21:45", false),
        )

        val fridayAt6oClock = LocalDateTime.of(2024, Month.JULY, 5, 6, 0)

        OpenStateMapper.getOpenState(openingHours, fridayAt6oClock) shouldBeEqualTo OpenState.Open("21:45")
    }

    @Test
    fun `take into account closesNextDay`() {
        // Zoetermeer Centrum West: a combination of closesNextDay true and false
        val openingHours = listOf(
            OpeningHours(1, "05:00", "24:00", false),
            OpeningHours(2, "05:00", "24:00", false),
            OpeningHours(3, "05:00", "24:00", false),
            OpeningHours(4, "05:00", "24:00", false),
            OpeningHours(5, "05:00", "01:00", true),
            OpeningHours(6, "06:30", "01:00", true),
            OpeningHours(7, "08:00", "24:00", false),
        )

        val friday0030 = LocalDateTime.of(2024, Month.JULY, 5, 0, 30)
        OpenStateMapper.getOpenState(openingHours, friday0030) shouldBeEqualTo OpenState.Closed(null, "05:00")

        val saturdayAt1030 = LocalDateTime.of(2024, Month.JULY, 6, 1, 30)
        OpenStateMapper.getOpenState(openingHours, saturdayAt1030) shouldBeEqualTo OpenState.Closed(null, "06:30")

        val saturdayAt0030 = LocalDateTime.of(2024, Month.JULY, 6, 0, 30)
        OpenStateMapper.getOpenState(openingHours, saturdayAt0030) shouldBeEqualTo OpenState.Closing("01:00")
    }

    @Test
    fun `don't bug out at 30 seconds before midnight`() {
        // Zoetermeer Driemanspolder
        val openingHours = listOf(
            OpeningHours(1, "05:00", "24:00", false),
            OpeningHours(2, "05:00", "24:00", false),
            OpeningHours(3, "05:00", "24:00", false),
            OpeningHours(4, "05:00", "24:00", false),
            OpeningHours(5, "05:00", "01:00", true),
            OpeningHours(6, "07:00", "01:00", true),
            OpeningHours(7, "08:30", "24:00", false),
        )


        val friday2359 = LocalDateTime.of(2024, Month.JULY, 5, 23, 58, 30)
        OpenStateMapper.getOpenState(openingHours, friday2359) shouldBeEqualTo OpenState.Open("01:00")

//        val thursday2359 = LocalDateTime.of(2024, Month.JULY, 4, 23, 59, 30)
//        OpenStateMapper.getOpenState(openingHours, thursday2359) shouldBeEqualTo OpenState.Closing("24:00")
//        val sunday2359 = LocalDateTime.of(2024, Month.JULY, 7, 23, 59, 30)
//        OpenStateMapper.getOpenState(openingHours, sunday2359) shouldBeEqualTo OpenState.Closing("24:00")
    }
}