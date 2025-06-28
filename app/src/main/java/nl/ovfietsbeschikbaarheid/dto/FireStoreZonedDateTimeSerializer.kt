package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FireStoreZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    private val dateTimePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.ovfietsbeschikbaarheid.BigDecimalSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(dateTimePattern.format(value))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return ZonedDateTime.parse(decoder.decodeString(), dateTimePattern)
    }
}