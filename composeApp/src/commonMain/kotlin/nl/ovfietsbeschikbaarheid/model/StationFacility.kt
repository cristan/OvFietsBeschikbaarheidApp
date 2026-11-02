package nl.ovfietsbeschikbaarheid.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wc
import androidx.compose.ui.graphics.vector.ImageVector
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.station_facility_aed
import nl.ovfietsbeschikbaarheid.resources.station_facility_bagagekluizen
import nl.ovfietsbeschikbaarheid.resources.station_facility_kaartautomaat
import nl.ovfietsbeschikbaarheid.resources.station_facility_lift
import nl.ovfietsbeschikbaarheid.resources.station_facility_reisassistentie
import nl.ovfietsbeschikbaarheid.resources.station_facility_servicewinkel
import nl.ovfietsbeschikbaarheid.resources.station_facility_toilet
import nl.ovfietsbeschikbaarheid.resources.station_facility_wachtruimte
import nl.ovfietsbeschikbaarheid.resources.station_facility_watertappunt
import org.jetbrains.compose.resources.StringResource

// The details screen lists the facilities in the order they are declared here.
enum class StationFacility(val textRes: StringResource, val icon: ImageVector) {
    Toilet(Res.string.station_facility_toilet, Icons.Filled.Wc),
    Lift(Res.string.station_facility_lift, Icons.Filled.Elevator),
    Kaartautomaat(Res.string.station_facility_kaartautomaat, Icons.Filled.ConfirmationNumber),
    Servicewinkel(Res.string.station_facility_servicewinkel, Icons.Filled.SupportAgent),
    Bagagekluizen(Res.string.station_facility_bagagekluizen, Icons.Filled.Luggage),
    Wachtruimte(Res.string.station_facility_wachtruimte, Icons.Filled.Chair),
    Watertappunt(Res.string.station_facility_watertappunt, Icons.Filled.WaterDrop),
    Reisassistentie(Res.string.station_facility_reisassistentie, Icons.AutoMirrored.Filled.Accessible),
    AED(Res.string.station_facility_aed, Icons.Filled.MonitorHeart),
}
