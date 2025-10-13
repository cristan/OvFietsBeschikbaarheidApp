package nl.ovfietsbeschikbaarheid

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import kotlin.time.measureTimedValue

class KtorApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
//        install(Logging) {
//            logger = object : Logger {
//                override fun log(message: String) {
//                    Timber.d(message)
//                }
//            }
//            level = LogLevel.ALL
//        }
        install(HttpCache)
    }

    suspend fun getLocations(): List<LocationDTO> {
        val (locations, timeTaken) = measureTimedValue {
            httpClient.get("https://storage.googleapis.com/ov-fiets-updates/locations.json").body<List<LocationDTO>>()
        }
        Timber.d("Loaded locations in $timeTaken")
        return locations
    }

    suspend fun getNSApiDetails(detailUri: String): DetailsDTO? {
        Timber.i("Loading $detailUri")
        val (result, timeTaken) = measureTimedValue {
            httpClient.get(detailUri)
        }
        Timber.d("Loaded $detailUri in $timeTaken")
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
        val (history, timeTaken) = measureTimedValue {
            val result = httpClient.post("https://firestore.googleapis.com/v1/projects/ov-fiets-app-427721/databases/(default)/documents:runQuery") {
                header("Content-Type", "application/json")
                setBody(body)
            }
            result.body<List<HourlyLocationCapacityDto>>()
        }
        Timber.d("Loaded the history of $code since $startDate in $timeTaken")
        return history
    }

    fun close() {
        httpClient.close()
    }
}