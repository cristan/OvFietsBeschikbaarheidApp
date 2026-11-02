package nl.ovfietsbeschikbaarheid.mapper

import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.model.DetailsSkeletonModel
import org.junit.Test
import kotlin.test.assertEquals

class DetailsSkeletonMapperTest {

    private val overviewLocations = Json.decodeFromString<List<LocationDTO>>(
        javaClass.getResource("/locations.json")!!.readText()
    )

    @Test
    fun `the skeleton has the texts, opening hours and alternatives of the location`() {
        val skeleton = DetailsSkeletonMapper.getDetailsSkeleton("amf002", overviewLocations)

        assertEquals(
            DetailsSkeletonModel(
                disruptions = null,
                directions = "Volg vanaf het station de borden Mondriaanplein. U vindt de stalling recht voor de uitgang van het station.",
                about = "Je huurt hier een OV-fiets met het nieuwe slot, waarbij je OV-chipkaart de sleutel is. Meer informatie vind je op ov-fiets.nl/slot.\r\n\r\nAan de andere kant van het station vind je de bemenste fietsenstalling. Ook hier kun je een OV-fiets huren.",
                openingHoursInfo = "Dit OV-fiets uitgiftepunt is open van een kwartier voor vertrek van de eerste trein tot een kwartier na aankomst van de laatste trein.",
                openingHoursDayCount = 7,
                alternativeCount = 1,
            ),
            skeleton
        )
    }

    @Test
    fun `the skeleton has the disruptions of the location`() {
        val skeleton = DetailsSkeletonMapper.getDetailsSkeleton("ndb003", overviewLocations)

        assertEquals(
            "Vrijdag 18 oktober kunt u van 9 tot 13 uur géén OV-fiets huren in de fietsenstalling Naarden-Bussum i.v.m. werkzaamheden.",
            skeleton.disruptions
        )
    }

    @Test
    fun `the skeleton has no opening hours when the location has none`() {
        val skeleton = DetailsSkeletonMapper.getDetailsSkeleton("eml001", overviewLocations)

        assertEquals(0, skeleton.openingHoursDayCount)
    }

    @Test
    fun `locations with station code BSLC have no alternatives`() {
        // Amsterdam Noord shares the station code BSLC with Maassluis
        val skeleton = DetailsSkeletonMapper.getDetailsSkeleton("asd009", overviewLocations)

        assertEquals(0, skeleton.alternativeCount)
    }

    @Test
    fun `the skeleton is the most typical one when the overview hasn't loaded`() {
        val skeleton = DetailsSkeletonMapper.getDetailsSkeleton("amf002", null)

        assertEquals(
            DetailsSkeletonModel(
                disruptions = null,
                directions = null,
                about = "Je huurt hier een OV-fiets met het nieuwe OV-fietsslot, waarbij je OV-chipkaart de sleutel is. Meer informatie vind je op ov-fiets.nl/slot.",
                openingHoursInfo = null,
                openingHoursDayCount = 7,
                alternativeCount = 0,
            ),
            skeleton
        )
    }
}
