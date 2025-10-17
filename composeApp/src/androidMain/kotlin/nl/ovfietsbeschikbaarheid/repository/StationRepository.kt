package nl.ovfietsbeschikbaarheid.repository

import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.dto.Station
import kotlin.time.measureTimedValue

class StationRepository(
    private val context: Context
) {
    private var cachedStations: Map<String, String>? = null

    private val json = Json

    /**
     * Returns a map between station code and station name
     */
    fun getAllStations(): Map<String, String> {
        cachedStations?.let {
            return it
        }
        val (stations, timeTaken) = measureTimedValue {
            val stationsStream = context.resources.openRawResource(R.raw.stations)
            val inputAsString = stationsStream.bufferedReader().use { it.readText() }
            json.decodeFromString<List<Station>>(inputAsString)
                .associate { it.code to it.name }
        }
        Logger.d("Loaded stations in $timeTaken")
        cachedStations = stations
        return stations
    }
}
