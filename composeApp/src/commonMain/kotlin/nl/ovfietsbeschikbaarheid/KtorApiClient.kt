package nl.ovfietsbeschikbaarheid

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.DetailsDTO
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import nl.ovfietsbeschikbaarheid.dto.LocationHistoryDTO
import nl.ovfietsbeschikbaarheid.repository.APP_COMPATIBILITY
import nl.ovfietsbeschikbaarheid.repository.AppCompatibilityRepository
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

    suspend fun getLocationHistory(locationCode: String): LocationHistoryDTO? = getHistoryFile("history/$locationCode.json")

    suspend fun getRecentHistory(): Map<String, LocationHistoryDTO>? = getHistoryFile("recent_history.json")

    private suspend inline fun <reified T> getHistoryFile(fileName: String): T? {
        Logger.i("Loading $fileName")
        return try {
            val (history, timeTaken) = measureTimedValue {
                httpClient.get("https://storage.googleapis.com/ov-fiets-updates/$fileName").body<T>()
            }
            Logger.d("Loaded $fileName in $timeTaken")
            history
        } catch (e: ClientRequestException) {
            if (e.response.status != HttpStatusCode.NotFound) throw e
            Logger.w("$fileName doesn't exist")
            null
        }
    }

    fun close() {
        httpClient.close()
    }
}