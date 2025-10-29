package nl.ovfietsbeschikbaarheid.ext

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime

val dutchTimeZone = TimeZone.of("Europe/Amsterdam")

fun LocalDateTime.atStartOfDay() = LocalDateTime(year, month.number, day, 0, 0, 0, 0)

fun LocalDateTime.atEndOfDay() = LocalDateTime(year, month.number, day, 23, 59, 59, 0)

@OptIn(ExperimentalTime::class)
fun LocalDateTime.atEndOfDayIn(timeZone: TimeZone) = this.atEndOfDay().toInstant(timeZone)

fun LocalDateTime.truncateToHour() = LocalDateTime(year, month.number, day, hour, 0, 0, 0)

fun LocalDateTime.truncateToDay() = LocalDateTime(year, month.number, day, 0, 0, 0, 0)

fun LocalDateTime.millisecondsUntil(other: LocalDateTime): Int {
    val milliseconds = this.hour * 24000 + this.hour * 3600000 + this.minute * 60000 + this.second * 1000 + (this.nanosecond / 1000)
    val otherMillis = other.hour * 24000 + other.hour * 3600000 + other.minute * 60000 + other.second * 1000 + (other.nanosecond / 1000)
    return otherMillis - milliseconds
}

fun LocalTime.minutesUntil(other: LocalTime): Int {
    val otherMinutes = other.hour * 60 + other.minute
    val thisMinutes = this.hour * 60 + this.minute
    return otherMinutes - thisMinutes
}

val LocalTime.Companion.MAX: LocalTime
    get() = LocalTime(23, 59, 59)

val LocalTime.Companion.MIN: LocalTime
    get() = LocalTime(0, 0)

fun LocalDate.toFullDayOfWeek() = when(this.dayOfWeek) {
    DayOfWeek.MONDAY -> "maandag"
    DayOfWeek.TUESDAY -> "dinsdag"
    DayOfWeek.WEDNESDAY -> "woensdag"
    DayOfWeek.THURSDAY -> "donderdag"
    DayOfWeek.FRIDAY -> "vrijdag"
    DayOfWeek.SATURDAY -> "zaterdag"
    DayOfWeek.SUNDAY ->  "zondag"
}

fun LocalDate.toNarrowDayOfWeek() = when(this.dayOfWeek) {
    DayOfWeek.MONDAY -> "M"
    DayOfWeek.TUESDAY -> "D"
    DayOfWeek.WEDNESDAY -> "W"
    DayOfWeek.THURSDAY -> "D"
    DayOfWeek.FRIDAY -> "V"
    DayOfWeek.SATURDAY -> "Z"
    DayOfWeek.SUNDAY -> "Z"
}

fun DayOfWeek.toIsoDayNumber(): Int = when (this) {
    DayOfWeek.MONDAY -> 1
    DayOfWeek.TUESDAY -> 2
    DayOfWeek.WEDNESDAY -> 3
    DayOfWeek.THURSDAY -> 4
    DayOfWeek.FRIDAY -> 5
    DayOfWeek.SATURDAY -> 6
    DayOfWeek.SUNDAY -> 7
}