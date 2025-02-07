package nl.ovfietsbeschikbaarheid.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import timber.log.Timber

data class VehicleModel(
    val vehiclePosition: LatLng,
    val formFactor: FormFactor,
    val systemId: String,
    val vehicleId: String
): ClusterItem {
    override fun getPosition() = vehiclePosition

    override fun getTitle() = formFactor.name

    override fun getSnippet() = systemId

    override fun getZIndex() = 0f

    fun getColor() = when (systemId) {
            "lime" -> Color(0xFF1bd831)
            "htm" -> Color(0xFFdc281e)
            "cykl" -> Color(0xFFa5e067)
            "donkey" -> Color(0xFFed5144)
            "gosharing" -> Color(0xFF3dbcc8)
            "baqme" -> Color(0xFF50e3c2)
            "hely" -> Color(0xFFfd645c)
            "deelfietsnederland" -> Color(0xFF00b0fe)
            "felyx" -> Color(0xFF064627)
            "bolt" -> Color(0xFF32bb78)
            "dott" -> Color(0xFF00a8e9)
            "moveyou" -> Color(0xFF13D6A6)
            else -> {
                Timber.w("Unexpected systemId: $systemId")
                Color.Black
            }
        }
}

enum class FormFactor {
    BICYCLE,
    CARGO_BICYCLE,
    MOPED,
}