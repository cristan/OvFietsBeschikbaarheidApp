package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class FireStoreInstantSerializer : KSerializer<Instant> {
    // Firebase can return different amounts of fractional second digits
    // 2025-07-18T22:00:53.145887Z
    // 2025-07-24T19:00:11.332Z

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.ovfietsbeschikbaarheid.FireStoreZonedDateTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        // Firestore expects full ISO-8601 with Z suffix
        encoder.encodeString(value.toString()) // Instant.toString() → e.g. "2025-07-18T22:00:53.145887Z"
    }

    override fun deserialize(decoder: Decoder): Instant {
        val raw = decoder.decodeString().trim()
        // Kotlinx datetime's Instant parser already handles:
        // - optional fractional seconds (.S, .SSS, .SSSSSS)
        // - required Z suffix or offset
        return Instant.parse(raw)
    }
}