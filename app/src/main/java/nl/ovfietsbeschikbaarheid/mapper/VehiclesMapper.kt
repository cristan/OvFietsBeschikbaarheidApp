package nl.ovfietsbeschikbaarheid.mapper

import com.google.android.gms.maps.model.LatLng
import nl.ovfietsbeschikbaarheid.dto.VehicleDTO
import nl.ovfietsbeschikbaarheid.model.FormFactor
import nl.ovfietsbeschikbaarheid.model.VehicleModel
import timber.log.Timber

object VehiclesMapper {
    fun map(vehicles: List<VehicleDTO>): List<VehicleModel> {
        val filtered = vehicles.filter {

            val shownTypes = it.form_factor == "bicycle" || it.form_factor == "cargo_bicycle"
            if(!shownTypes && it.form_factor != "car" && it.form_factor != "moped") {
                Timber.w("Unexpected form factor: ${it.form_factor}")
            }
            return@filter shownTypes &&
                    !it.is_disabled &&
                    !it.is_reserved &&
                    !(it.lat == 0.0 && it.lon == 0.0)
            // TODO: filter out coordinates who aren't in the Netherlands or Belgium (sometimes you get a random one in America or Europe or something)
            //  This also filters out the ones at 0,0
        }
        return filtered.map {
            val formFactor = when (it.form_factor) {
                "bicycle" -> FormFactor.BICYCLE
                "cargo_bicycle" -> FormFactor.CARGO_BICYCLE
                "moped" -> FormFactor.MOPED
                else -> throw IllegalArgumentException("Unknown form factor: ${it.form_factor}")
            }
            VehicleModel(LatLng(it.lat, it.lon), formFactor, it.system_id, it.vehicle_id)
        }
    }
}