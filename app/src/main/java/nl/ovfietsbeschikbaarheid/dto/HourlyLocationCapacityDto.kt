@file:UseSerializers(FireStoreZonedDateTimeSerializer::class)

package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime

@Serializable
data class HourlyLocationCapacityDto(
    val readTime: String,
    val document: HourlyLocationDocumentDto
)

@Serializable
data class HourlyLocationDocumentDto(
    val name: String,
    val fields: HourlyLocationFieldsDto,
    val createTime: ZonedDateTime,
    val updateTime: ZonedDateTime,
)

@Serializable
data class HourlyLocationFieldsDto(
    val hour: StringValueContainerDto,
    val first: IntegerValueContainerDto,
)

@Serializable
data class StringValueContainerDto(
    val stringValue: String
)

@Serializable
data class IntegerValueContainerDto(
    val integerValue: String
)