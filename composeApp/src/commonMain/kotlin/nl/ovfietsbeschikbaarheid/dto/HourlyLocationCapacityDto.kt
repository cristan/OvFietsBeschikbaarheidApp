@file:UseSerializers(FireStoreInstantSerializer::class)

package nl.ovfietsbeschikbaarheid.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class HourlyLocationCapacityDto(
    val readTime: String,
    val document: HourlyLocationDocumentDto
)

@OptIn(ExperimentalTime::class)
@Serializable
data class HourlyLocationDocumentDto(
    val name: String,
    val fields: HourlyLocationFieldsDto,
    val createTime: Instant,
    val updateTime: Instant,
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