package nl.ovfietsbeschikbaarheid.ext

import java.time.ZonedDateTime

fun ZonedDateTime.atStartOfDay() = this.withHour(0).withMinute(0).withSecond(0).withNano(0)