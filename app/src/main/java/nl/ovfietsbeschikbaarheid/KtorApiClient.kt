package nl.ovfietsbeschikbaarheid

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.DetailsDTO
import nl.ovfietsbeschikbaarheid.dto.HourlyLocationCapacityDto
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import timber.log.Timber

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

    suspend fun getLocations(): List<LocationDTO> {
        return httpClient.get("https://storage.googleapis.com/ov-fiets-updates/locations.json").body<List<LocationDTO>>()
    }

    suspend fun getDetails(detailUri: String): DetailsDTO? {
        Timber.i("Loading $detailUri")
        val result = httpClient.get(detailUri)
        if (result.status.value == 404) {
            return null
        }
        return result.body<DetailsDTO>()
    }

    suspend fun getHistory(code: String, startDate: String): List<HourlyLocationCapacityDto> {
        Timber.i("Loading the history of $code")
        val body = """
        {
            "structuredQuery": {
                "from": [
                    {
                        "collectionId": "hourly_location_capacity"
                    }
                ],
                "select": {
                    "fields": [
                        {
                            "fieldPath": "hour"
                        },
                        {
                            "fieldPath": "first"
                        }
                    ]
                },
               "where": {
                    "compositeFilter": {
                        "op": "AND",
                        "filters": [
                            {
                                "fieldFilter": {
                                    "field": {
                                        "fieldPath": "code"
                                    },
                                    "op": "EQUAL",
                                    "value": {
                                        "stringValue": "$code"
                                    }
                                }
                            },
                            {
                                "fieldFilter": {
                                    "field": {
                                        "fieldPath": "timestamp"
                                    },
                                    "op": "GREATER_THAN_OR_EQUAL",
                                    "value": {
                                        "timestampValue": "$startDate"
                                    }
                                }
                            }
                        ]
                    }
                }
            }
        }
        """.trimIndent()
        val result = httpClient.post("https://firestore.googleapis.com/v1/projects/ov-fiets-app-427721/databases/(default)/documents:runQuery") {
            header("Content-Type", "application/json")
            setBody(body)
        }
//        if (result.status.value == 404) {
//            return null
//        }
        return result.body<List<HourlyLocationCapacityDto>>()
    }

    fun close() {
        httpClient.close()
    }
}