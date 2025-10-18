package nl.ovfietsbeschikbaarheid.util

import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.ovfietsbeschikbaarheid.repository.RatingStorageRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RatingEligibilityService(
    private val ratingStorageRepository: RatingStorageRepository
) {

    companion object {
        private const val REQUIRED_UNIQUE_VISITS = 7
        private const val DAYS_BETWEEN_REQUESTS = 180
    }

    suspend fun shouldRequestRating(now: Instant = Clock.System.now()): Boolean {
        val visitDates = ratingStorageRepository.dailyVisitDates.first()
        if (visitDates.size < REQUIRED_UNIQUE_VISITS) {
            return false
        }
        val lastRatingPromptTimestamp = ratingStorageRepository.lastRatingPromptTimestamp.first()

        if (lastRatingPromptTimestamp == null) {
            return true
        }

        val daysSinceLastPrompt = (now - Instant.fromEpochMilliseconds(lastRatingPromptTimestamp)).inWholeDays
        return daysSinceLastPrompt < DAYS_BETWEEN_REQUESTS
    }

    suspend fun onGpsContentViewed() {
        ratingStorageRepository.recordNewVisit(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }

    suspend fun onRatingPrompted() {
        ratingStorageRepository.recordRatingPromptedAndClearDailyVisits(Clock.System.now())
    }
}