package com.ovfietsbeschikbaarheid

import android.content.ContentValues.TAG
import android.util.Log
import com.ovfietsbeschikbaarheid.dto.DetailsDTO
import com.ovfietsbeschikbaarheid.dto.DetailsPayload
import com.ovfietsbeschikbaarheid.dto.Location
import com.ovfietsbeschikbaarheid.dto.LocationsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.math.log

class KtorApiClient {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    suspend fun getLocations(): LocationsDTO {
        return httpClient.get("http://fiets.openov.nl/locaties.json").body<LocationsDTO>()
    }

    suspend fun getDetails(detailUri: String): DetailsDTO {
        Log.i(TAG, "Loading $detailUri")
        return httpClient.get(detailUri).body<DetailsDTO>()
    }

    fun close() {
        httpClient.close()
    }
}