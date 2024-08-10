package com.ovfietsbeschikbaarheid.repository

import android.content.Context
import androidx.compose.ui.text.toUpperCase
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.ovfietsbeschikbaarheid.R
import java.util.Locale

object StationRepository {
    private var cachedStations: Map<String, String>? = null

    // allLocations will be empty after app is recreated. This works around that, but there's probably a nicer way to do this.
    fun getAllStations(context: Context): Map<String, String> {
        val allStations = cachedStations
        if (allStations != null) {
            return allStations
        }
        val stationsStream = context.resources.openRawResource(R.raw.stations_nl_2015_08)
        val stations = HashMap<String, String>()
        csvReader{delimiter = ';'}.readAll(stationsStream).forEach {
            val code = it[1].uppercase(Locale.UK)
            val stationName = it[3]
            stations[code] = stationName
        }
        cachedStations = stations
        return stations
    }
}
