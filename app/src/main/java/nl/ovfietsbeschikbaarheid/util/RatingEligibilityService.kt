package nl.ovfietsbeschikbaarheid.util

import kotlinx.coroutines.flow.first
import nl.ovfietsbeschikbaarheid.repository.RatingStorageRepository
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class RatingEligibilityService(
    private val ratingStorageRepository: RatingStorageRepository
) {

    companion object {
        private const val REQUIRED_UNIQUE_VISITS = 7
        private const val DAYS_BETWEEN_REQUESTS = 180
    }

    suspend fun shouldRequestRating(now: Instant = Instant.now()): Boolean {
        val visitDates = ratingStorageRepository.dailyVisitDates.first()
        if (visitDates.size < REQUIRED_UNIQUE_VISITS) {
            return false
        }
        val lastRatingPromptTimestamp = ratingStorageRepository.lastRatingPromptTimestamp.first()

        if (lastRatingPromptTimestamp == null) {
            return true
        }

        val daysSinceLastPrompt = ChronoUnit.DAYS.between(Instant.ofEpochMilli(lastRatingPromptTimestamp), now)
        return daysSinceLastPrompt < DAYS_BETWEEN_REQUESTS
    }

    suspend fun onGpsContentViewed() {
        ratingStorageRepository.recordNewVisit(LocalDate.now())
    }

    suspend fun onRatingPrompted() {
        ratingStorageRepository.recordRatingPromptedAndClearDailyVisits(Instant.now())
    }
}