package nl.ovfietsbeschikbaarheid.mapper

import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.StationFacilitiesDTO
import nl.ovfietsbeschikbaarheid.model.StationFacility
import org.junit.Test
import kotlin.test.assertEquals

class StationFacilitiesMapperTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `NS facilities without a matching StationFacility are left out`() {
        // BET.json also has Geleidelijnen, OV-chipkaart poorten, OV-fiets, Onbemenst fietsparkeren, P+R gratis and Service- en alarmzuil.
        val stationFacilities = StationFacilitiesMapper.convert(readStationFacilities("BET"))

        assertEquals(
            listOf(
                StationFacility.Toilet,
                StationFacility.Lift,
                StationFacility.Kaartautomaat,
                StationFacility.Wachtruimte,
                StationFacility.Watertappunt,
                StationFacility.Reisassistentie,
                StationFacility.AED,
            ),
            stationFacilities
        )
    }

    @Test
    fun `a SANIFAIR toilet counts as a toilet`() {
        val stationFacilities = StationFacilitiesMapper.convert(readStationFacilities("ASD"))

        assertEquals(
            listOf(
                StationFacility.Toilet,
                StationFacility.Lift,
                StationFacility.Kaartautomaat,
                StationFacility.Servicewinkel,
                StationFacility.Bagagekluizen,
                StationFacility.Wachtruimte,
                StationFacility.Watertappunt,
                StationFacility.Reisassistentie,
                StationFacility.AED,
            ),
            stationFacilities
        )
    }

    @Test
    fun `Arriva ticket machines and travel assistance are included`() {
        val stationFacilities = StationFacilitiesMapper.convert(readStationFacilities("MMLH"))

        assertEquals(
            listOf(
                StationFacility.Kaartautomaat,
                StationFacility.Reisassistentie,
                StationFacility.AED,
            ),
            stationFacilities
        )
    }

    private fun readStationFacilities(stationCode: String): StationFacilitiesDTO {
        val response = javaClass.getResource("/station-facilities/$stationCode.json")!!.readText()
        return json.decodeFromString(response)
    }
}
