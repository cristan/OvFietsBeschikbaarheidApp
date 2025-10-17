package nl.ovfietsbeschikbaarheid.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class RatingStorageRepository(private val dataStore: DataStore<Preferences>) {

    private object RatingPrefKeys {
        val DAILY_VISIT_DATES = stringSetPreferencesKey("rating_daily_visit_dates")
        val LAST_RATING_PROMPT_TIMESTAMP = longPreferencesKey("rating_last_prompt_timestamp")
    }

    val dailyVisitDates: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[RatingPrefKeys.DAILY_VISIT_DATES] ?: emptySet()
        }

    suspend fun recordNewVisit(today: LocalDate) {
        dataStore.edit {
            val today = today.toString()
            val currentDates = it[RatingPrefKeys.DAILY_VISIT_DATES] ?: emptySet()
            if (!currentDates.contains(today)) {
                it[RatingPrefKeys.DAILY_VISIT_DATES] = currentDates + today
            }
        }
    }

    val lastRatingPromptTimestamp: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[RatingPrefKeys.LAST_RATING_PROMPT_TIMESTAMP]
        }

    /**
     * Records the last time the rating prompt was shown.
     * The daily visits are cleared too, because we want to have X visits again before we prompt the user again for a rating.
     */
    suspend fun recordRatingPromptedAndClearDailyVisits(now: Instant) {
        dataStore.edit { settings ->
            settings[RatingPrefKeys.LAST_RATING_PROMPT_TIMESTAMP] = now.toEpochMilliseconds()
            settings[RatingPrefKeys.DAILY_VISIT_DATES] = emptySet()
        }
    }
}