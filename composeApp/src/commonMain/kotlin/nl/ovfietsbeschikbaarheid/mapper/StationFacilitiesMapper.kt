package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.dto.StationFacilitiesDTO
import nl.ovfietsbeschikbaarheid.model.StationFacility

object StationFacilitiesMapper {
    fun convert(stationFacilitiesDTO: StationFacilitiesDTO): List<StationFacility> {
        return stationFacilitiesDTO.payload
            .mapNotNull { getStationFacility(it.name) }
            .distinct()
            .sorted()
    }

    private fun getStationFacility(nsName: String): StationFacility? {
        return when (nsName) {
            "Toilet", "SANIFAIR Toilet" -> StationFacility.Toilet
            "Lift" -> StationFacility.Lift
            "Kaartautomaat NS", "Kaartautomaat Arriva", "Kaartautomaat Blauwnet" -> StationFacility.Kaartautomaat
            "OV Service & Tickets NS",
            "OV Servicewinkel Arriva",
            "OV Servicewinkel Connexxion",
            "OV Servicewinkel HTM",
            "OV Servicewinkel RET" -> StationFacility.Servicewinkel
            "Bagagekluizen" -> StationFacility.Bagagekluizen
            "Wachtruimte" -> StationFacility.Wachtruimte
            "Watertappunt" -> StationFacility.Watertappunt
            "Reisassistentie NS",
            "Reisassistentie Arriva",
            "Reisassistentie Blauwnet",
            "Reisassistentie Breng/Arriva",
            "Reisassistentie Qbuzz",
            "Reisassistentie R-Net",
            "Reisassistentie RRReis (Keolis)" -> StationFacility.Reisassistentie
            "AED" -> StationFacility.AED
            else -> null
        }
    }
}
