package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class FireStoreZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    // Firebase can return different amounts of fractional second digits
    // 2025-07-18T22:00:53.145887Z
    // 2025-07-24T19:00:11.332Z

    private val flexibleFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true) // optional .SSS to .SSSSSS
        .appendOffsetId()
        .toFormatter()

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.ovfietsbeschikbaarheid.FireStoreZonedDateTimeSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(flexibleFormatter.format(value))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return ZonedDateTime.parse(decoder.decodeString(), flexibleFormatter)
    }
}