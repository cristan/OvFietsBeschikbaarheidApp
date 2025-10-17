package nl.ovfietsbeschikbaarheid.repository

import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.Station
import nl.ovfietsbeschikbaarheid.resources.Res
import kotlin.time.measureTimedValue

class StationRepository() {
    private var cachedStations: Map<String, String>? = null

    private val json = Json

    /**
     * Returns a map between station code and station name
     */
    suspend fun getAllStations(): Map<String, String> {
        cachedStations?.let {
            return it
        }
        val (stations, timeTaken) = measureTimedValue {
            val stationsJsonString = Res.readBytes("files/stations.json").decodeToString()
            json.decodeFromString<List<Station>>(stationsJsonString)
                .associate { it.code to it.name }
        }

        Logger.d("Loaded stations in $timeTaken")
        cachedStations = stations
        return stations
    }
}
