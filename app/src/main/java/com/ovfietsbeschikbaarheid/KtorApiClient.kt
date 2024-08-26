package com.ovfietsbeschikbaarheid

import android.content.ContentValues.TAG
import android.util.Log
import com.ovfietsbeschikbaarheid.dto.DetailsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    suspend fun getDetails(detailUri: String): DetailsDTO {
        Log.i(TAG, "Loading $detailUri")
        return httpClient.get(detailUri).body<DetailsDTO>()
    }

    fun close() {
        httpClient.close()
    }
}