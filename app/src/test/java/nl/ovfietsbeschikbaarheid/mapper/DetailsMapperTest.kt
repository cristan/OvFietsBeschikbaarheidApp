package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.dto.OpeningHoursDTO
import nl.ovfietsbeschikbaarheid.model.OpenState
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class DetailsMapperTest {
    @Test
    fun `happy flow`() {
        val openingHours = (1..7).map {
            OpeningHoursDTO(it, "00:00", "24:00", true)
        }
        assertEquals(OpenState.Open247, OpenStateMapper.getOpenState("ana001", openingHours, LocalDateTime.now()))
    }
}