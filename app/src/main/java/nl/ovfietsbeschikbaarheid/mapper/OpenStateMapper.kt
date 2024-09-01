package nl.ovfietsbeschikbaarheid.mapper

import nl.ovfietsbeschikbaarheid.dto.OpeningHours
import nl.ovfietsbeschikbaarheid.model.OpenState
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Locale

object OpenStateMapper {
    fun getOpenState(openingHours: List<OpeningHours>, dateTime: LocalDateTime): OpenState {
        if (
            openingHours.all { it.startTime == "00:00" && it.endTime == "24:00" } ||
            openingHours.all { it.startTime == "00:00" && it.endTime == "00:01" } ||
            openingHours.all { it.startTime == "00:00" && it.endTime == "23:59" }
        ) {
            return OpenState.Open247
        }
        // Monday is 1, Sunday is 7, same as what is returned from the API
        val today = dateTime.dayOfWeek.value

        val yesterdayOpeningHours = openingHours.find {
            val dayYesterday = if (today == 1) 7 else today - 1
            if (it.dayOfWeek == dayYesterday && it.closesNextDay) {
                dateTime.toLocalTime() < LocalTime.parse(it.endTime)
            } else {
                false
            }
        }
        if (yesterdayOpeningHours != null) {
            val minutesUntilClosing = dateTime.toLocalTime().until(LocalTime.parse(yesterdayOpeningHours.endTime), ChronoUnit.MINUTES)
            return if (minutesUntilClosing < 60) {
                OpenState.Closing(yesterdayOpeningHours.endTime)
            } else {
                OpenState.Open(yesterdayOpeningHours.endTime)
            }
        }

        val todayOpeningHours = openingHours.find {
            if (it.dayOfWeek == today) {
                val afterStartTime = it.startTime == "00:00" || dateTime.toLocalTime() >= LocalTime.parse(it.startTime)
                val beforeEndTime = it.endTime == "24:00" || it.closesNextDay || dateTime.toLocalTime() <= LocalTime.parse(it.endTime)
                afterStartTime && beforeEndTime
            } else {
                false
            }
        }
        if (todayOpeningHours != null) {
            val endTime = if(todayOpeningHours.endTime == "24:00") LocalTime.MAX else LocalTime.parse(todayOpeningHours.endTime)
            val minutesUntilClosing = if (todayOpeningHours.closesNextDay) {
                // Until the end of the day + the hours still open after 24:00 hours
                dateTime.toLocalTime().until(LocalTime.MAX, ChronoUnit.MINUTES) + LocalTime.MIN.until(endTime, ChronoUnit.MINUTES)
            } else {
                dateTime.toLocalTime().until(endTime, ChronoUnit.MINUTES)
            }
            return if (minutesUntilClosing < 60) {
                OpenState.Closing(todayOpeningHours.endTime)
            } else {
                OpenState.Open(todayOpeningHours.endTime)
            }
        }

        val todayOpen = openingHours.find {
            it.dayOfWeek == today && dateTime.toLocalTime() < LocalTime.parse(it.startTime)
        }
        if (todayOpen != null) {
            return OpenState.Closed(openDay = null, todayOpen.startTime)
        }
        val nextDayInWeek = (today..7).firstNotNullOfOrNull { day ->
            openingHours.find { it.dayOfWeek == day }
        }
        val monday = openingHours.find { it.dayOfWeek == 1 }
        val nextDayOpen = nextDayInWeek ?: monday !!
        val opensTodayOrTomorrow = (today == 7 && nextDayOpen.dayOfWeek == 1) || (nextDayOpen.dayOfWeek == today) || (nextDayOpen.dayOfWeek - today == 1)
        return if (opensTodayOrTomorrow) {
            OpenState.Closed(openDay = "morgen", nextDayOpen.startTime)
        } else {
            OpenState.Closed(openDay = DetailsMapper.getDayName(nextDayOpen.dayOfWeek).lowercase(Locale.UK), nextDayOpen.startTime)
        }
    }
}