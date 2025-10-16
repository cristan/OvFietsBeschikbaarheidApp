package nl.ovfietsbeschikbaarheid.model

import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.baseline_key_24
import nl.ovfietsbeschikbaarheid.resources.baseline_person_24
import nl.ovfietsbeschikbaarheid.resources.fietskluizen_icon
import nl.ovfietsbeschikbaarheid.resources.garage_home_24dp
import nl.ovfietsbeschikbaarheid.resources.service_type_box
import nl.ovfietsbeschikbaarheid.resources.service_type_key_box
import nl.ovfietsbeschikbaarheid.resources.service_type_key_selfservice
import nl.ovfietsbeschikbaarheid.resources.service_type_lockers
import nl.ovfietsbeschikbaarheid.resources.service_type_manned
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class DetailsModel(
    val description: String,
    val openState: OpenState?,
    val openingHoursInfo: String?,
    val openingHours: List<OpeningHoursModel>,
    val rentalBikesAvailable: Int?,
    val capacity: Int,
    val serviceType: ServiceType?,
    val about: String?,
    val disruptions: String?,
    val directions: String?,
    val location: AddressModel?,
    val coordinates: LatLng,
    val stationName: String?,
    val alternatives: List<DetailScreenData>,
    val graphDays: List<GraphDayModel>,
)

data class GraphDayModel (
    val isToday: Boolean,
    val dayShortName: String,
    val dayFullName: String,
    val capacityHistory: List<CapacityModel>,
    val capacityPrediction: List<CapacityModel>,
    val contentDescription: String,
)

@OptIn(ExperimentalTime::class)
data class CapacityModel(
    val capacity: Int,
    val createTime: Instant
)

data class AddressModel(
    val city: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
)

data class OpeningHoursModel(
    val dayOfWeek: StringResource,
    val startTime: String,
    val endTime: String,
)

sealed class OpenState {
    data object Open247 : OpenState()
    data class Open(val closingTime: String) : OpenState()
    data class Closing(val closingTime: String) : OpenState()
    data class Closed(val openDay: StringResource?, val openTime: String) : OpenState()
}

enum class ServiceType(val textRes: StringResource, val icon: DrawableResource) {
    Bemenst(Res.string.service_type_manned, Res.drawable.baseline_person_24),
    // See: https://www.ns.nl/fietsenstallingen/abonnementen/fietskluizen.html
    Kluizen(Res.string.service_type_lockers, Res.drawable.fietskluizen_icon),
    // No idea what this is, but there are only 2, so it doesn't matter that much
    Box(Res.string.service_type_box, Res.drawable.fietskluizen_icon),
    Sleutelautomaat(Res.string.service_type_key_box, Res.drawable.baseline_key_24),
    // Example: https://www.debeeldunie.nl/stock-photo-nederland-cuijk-14-07-2015-fietsenstalling-voor-ov-fietsen-op-reportage-image00157529.html
    Zelfservice(Res.string.service_type_key_selfservice, Res.drawable.garage_home_24dp),
}