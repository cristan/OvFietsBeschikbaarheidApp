package nl.ovfietsbeschikbaarheid.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.model.Favourite

class FavouritesRepository(private val dataStore: DataStore<Preferences>) {

    private val favouritesKey = stringPreferencesKey("favourites")

    val favourites: Flow<List<Favourite>> = dataStore.data
        .map { preferences ->
            getFavourites(preferences)
        }

    suspend fun setFavourite(favourite: Favourite, isFavourite: Boolean) {
        dataStore.edit { preferences ->
            val otherFavourites = getFavourites(preferences).filter { it.locationCode != favourite.locationCode }
            preferences[favouritesKey] = Json.encodeToString(
                if (isFavourite) otherFavourites + favourite else otherFavourites
            )
        }
    }

    private fun getFavourites(preferences: Preferences): List<Favourite> =
        preferences[favouritesKey]?.let { Json.decodeFromString<List<Favourite>>(it) } ?: emptyList()
}
