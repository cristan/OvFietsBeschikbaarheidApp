package nl.ovfietsbeschikbaarheid

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.DetailsDTO
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.dto.LocationHistoryDTO
import nl.ovfietsbeschikbaarheid.repository.APP_COMPATIBILITY
import nl.ovfietsbeschikbaarheid.repository.AppCompatibilityRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class KtorApiClient(
    private val appCompatibilityRepository: AppCompatibilityRepository
) {

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
//                    Logger.d(message)
//                }
//            }
//            level = LogLevel.ALL
//        }
        install(HttpCache)
        expectSuccess = true
    }

    suspend fun getLocations(): List<LocationDTO> {
        val (locations, timeTaken) = measureTimedValue {
            val response = httpClient.get("https://storage.googleapis.com/ov-fiets-updates/locations.json")
            val minimumAppCompatibility = response.headers["x-goog-meta-minimum-app-compatibility"]?.toInt()
            if (minimumAppCompatibility != null && APP_COMPATIBILITY < minimumAppCompatibility) {
                appCompatibilityRepository.markUpdateRequired()
                awaitCancellation()
            }
            response.body<List<LocationDTO>>()
        }
        Logger.d("Loaded OV-fiets locations in $timeTaken")
        return locations
    }

    suspend fun getNSApiDetails(detailUri: String): DetailsDTO? {
        Logger.i("Loading $detailUri")
        val (result, timeTaken) = measureTimedValue {
            httpClient.get(detailUri)
        }
        Logger.d("Loaded $detailUri in $timeTaken")
        if (result.status.value == 404) {
            return null
        }
        return result.body<DetailsDTO>()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun getLocationHistory(locationCode: String): LocationHistoryDTO? = BundledHistory.getLocationHistory(locationCode, Clock.System.now())

    suspend fun getRecentHistory(): Map<String, LocationHistoryDTO>? = null

    fun close() {
        httpClient.close()
    }
}