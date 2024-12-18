package nl.ovfietsbeschikbaarheid.repository

import android.content.Context
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.dto.Station

class StationRepository(
    private val context: Context
) {
    private var cachedStations: Map<String, String>? = null
    private var cachedCapacities: Map<String, Int>? = null

    private val json = Json

    /**
     * Returns a map between station code and station name
     */
    fun getAllStations(): Map<String, String> {
        cachedStations?.let {
            return it
        }
        val stationsStream = context.resources.openRawResource(R.raw.stations)
        val inputAsString = stationsStream.bufferedReader().use { it.readText() }
        return json.decodeFromString<List<Station>>(inputAsString)
            .associate { it.code to it.name }
    }

    /**
     * Returns a map between location code and the station's capacity.
     * Note that the location code is always lowercase here, which it isn't always at other instances (Prinsendam is has the code Rtd003 for example)
     */
    fun getCapacities(): Map<String, Int> {
        cachedCapacities?.let {
            return it
        }
        val maxAvailableStream = context.resources.openRawResource(R.raw.max_2017_2023)
        val capabilities = HashMap<String, Int>()
        csvReader { delimiter = ';' }.readAll(maxAvailableStream)
            .drop(1)
            .forEach { line ->
                val code = line[0]
                val maxAvailabilities = line
                    .drop(2)
                    .map { maxAvailability ->
                        if (maxAvailability.isEmpty()) {
                            0
                        } else {
                            maxAvailability.toInt()
                        }
                    }
                capabilities[code] = maxAvailabilities.max()
            }
        cachedCapacities = capabilities
        return capabilities
    }
}
