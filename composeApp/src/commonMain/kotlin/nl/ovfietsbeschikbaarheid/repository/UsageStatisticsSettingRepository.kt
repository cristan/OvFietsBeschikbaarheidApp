package nl.ovfietsbeschikbaarheid.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UsageStatisticsSettingRepository(private val dataStore: DataStore<Preferences>) {

    private val isEnabledKey = booleanPreferencesKey("usage_statistics_enabled")

    val isEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[isEnabledKey] ?: true
        }

    suspend fun setEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[isEnabledKey] = isEnabled
        }
    }
}
