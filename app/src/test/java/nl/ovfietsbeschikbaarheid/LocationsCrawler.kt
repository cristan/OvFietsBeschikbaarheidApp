package nl.ovfietsbeschikbaarheid

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Not a test, rather a tool. Added in the test folder to prevent it being added in the app.
 */
object LocationsCrawler {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        val loaded = httpClient.get("http://fiets.openov.nl/locaties.json").body<LocationsDTO>()
        val asJson = json.encodeToString(loaded)
        val file = File("app/src/main/res/raw/locations.json")
        file.writeText(asJson)
    }
}

@Serializable
data class LocationsDTO(
    val locaties: Map<String, Location>
)

@Serializable
data class Location(
    val description: String,
    val stationCode: String,
    val lat: Double,
    val lng: Double,
    val extra: LocationExtra,
    val link: Link
)

@Serializable
data class Link(
    val uri: String
)

@Serializable
data class LocationExtra(
    val locationCode: String,
    val fetchTime: Long,
)