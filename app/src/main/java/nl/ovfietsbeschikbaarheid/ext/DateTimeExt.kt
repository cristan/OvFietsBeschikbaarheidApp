package nl.ovfietsbeschikbaarheid.ext

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun ZonedDateTime.atStartOfDay(): ZonedDateTime = this.truncatedTo(ChronoUnit.DAYS)

fun ZonedDateTime.atEndOfDay() = this.withHour(23).withMinute(59).withSecond(59).withNano(0)