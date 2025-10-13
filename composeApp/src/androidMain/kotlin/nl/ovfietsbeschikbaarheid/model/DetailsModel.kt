package nl.ovfietsbeschikbaarheid.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.R
import java.time.ZonedDateTime

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

data class CapacityModel(
    val capacity: Int,
    val dateTime: ZonedDateTime
)

data class AddressModel(
    val city: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
)

data class OpeningHoursModel(
    @StringRes
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
)

sealed class OpenState {
    data object Open247 : OpenState()
    data class Open(val closingTime: String) : OpenState()
    data class Closing(val closingTime: String) : OpenState()
    data class Closed(@StringRes val openDay: Int?, val openTime: String) : OpenState()
}

enum class ServiceType(@StringRes val textRes: Int, @DrawableRes val icon: Int) {
    Bemenst(R.string.service_type_manned, R.drawable.baseline_person_24),
    // See: https://www.ns.nl/fietsenstallingen/abonnementen/fietskluizen.html
    Kluizen(R.string.service_type_lockers, R.drawable.fietskluizen_icon),
    // No idea what this is, but there are only 2, so it doesn't matter that much
    Box(R.string.service_type_box, R.drawable.fietskluizen_icon),
    Sleutelautomaat(R.string.service_type_key_box, R.drawable.baseline_key_24),
    // Example: https://www.debeeldunie.nl/stock-photo-nederland-cuijk-14-07-2015-fietsenstalling-voor-ov-fietsen-op-reportage-image00157529.html
    Zelfservice(R.string.service_type_key_selfservice, R.drawable.garage_home_24dp),
}