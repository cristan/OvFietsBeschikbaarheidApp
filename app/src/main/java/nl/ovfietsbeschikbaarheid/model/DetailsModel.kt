package nl.ovfietsbeschikbaarheid.model

import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.R

data class DetailsModel(
    val description: String,
    val openState: OpenState?,
    val openingHours: List<OpeningHoursModel>,
    val rentalBikesAvailable: Int?,
    val capacity: Int,
    val serviceType: ServiceType?,
    val about: String?,
    val directions: String?,
    val location: LocationModel?,
    val coordinates: LatLng,
    val stationName: String?,
    val alternatives: List<LocationOverviewModel>,
)

data class LocationModel(
    val city: String,
    val street: String,
    val houseNumber: String,
    val postalCode: String,
)

data class OpeningHoursModel(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
)

sealed class OpenState {
    data object Open247 : OpenState()
    data class Open(val closingTime: String) : OpenState()
    data class Closing(val closingTime: String) : OpenState()
    data class Closed(val openDay: String?, val openTime: String) : OpenState()
}

enum class ServiceType(val text: String, @DrawableRes val icon: Int) {
    Bemenst("Bemenst", R.drawable.baseline_person_24),
    // See: https://www.ns.nl/fietsenstallingen/abonnementen/fietskluizen.html
    Kluizen("Kluizen", R.drawable.fietskluizen_icon),
    // No idea what this is, but there are only 2, so it doesn't matter that much
    Box("Box", R.drawable.fietskluizen_icon),
    Sleutelautomaat("Sleutelautomaat", R.drawable.baseline_key_24),
    // Example: https://www.debeeldunie.nl/stock-photo-nederland-cuijk-14-07-2015-fietsenstalling-voor-ov-fietsen-op-reportage-image00157529.html
    Zelfservice("Zelfservice", R.drawable.garage_home_24dp),
}