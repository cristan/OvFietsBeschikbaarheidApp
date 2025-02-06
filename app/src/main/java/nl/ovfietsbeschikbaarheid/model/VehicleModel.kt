package nl.ovfietsbeschikbaarheid.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class VehicleModel(
    val vehiclePosition: LatLng,
    val formFactor: FormFactor,
    val systemId: String,
    val vehicleId: String
): ClusterItem {
    override fun getPosition() = vehiclePosition

    override fun getTitle() = formFactor.name

    override fun getSnippet() = vehicleId

    override fun getZIndex() = 0f
}

enum class FormFactor {
    BICYCLE,
    CARGO_BICYCLE,
    MOPED,
}