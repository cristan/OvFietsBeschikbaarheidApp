package nl.ovfietsbeschikbaarheid

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

/**
 * Not a test, rather a tool. Added in the test folder to prevent it being added in the app.
 */
object StationsDownloader {
    private val json = Json {
        ignoreUnknownKeys = true
        // Pretty print would help in git diff, but it rarely changes and is otherwise a waste of space and CPU cycles
//        prettyPrint = true
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        if(args.isEmpty()) {
            println("No arguments provided! Add your subscription key https://apiportal.ns.nl/ as the first parameter.")
            exitProcess(1)
        }

        val subscriptionKey = args[0]

        // Alternative: download from https://data.ndovloket.nl/haltes/ It's a big blob of XML and it also contains needless stuff like
        // bus stops, but it seems to have a little more info like the street name.
        val loaded = httpClient.get("https://gateway.apiportal.ns.nl/nsapp-stations/v3") {
            this.header(
                "Ocp-Apim-Subscription-Key",
                subscriptionKey
            )
        }.body<StationsDTO>()
        val inNlMapped = loaded.payload
            .filter { it.country == "NL" }
            .map { MyStationDTO(it.id.code, it.names.long, it.location.lat, it.location.lng) }

        val asJson = json.encodeToString(inNlMapped)
        val file = File("app/src/main/res/raw/stations.json")
        file.writeText(asJson)
    }
}

@Serializable
data class MyStationDTO(
    val code: String,
    val name: String,
    val lat: Double,
    val lng: Double,
)

@Serializable
data class StationsDTO(
    val payload: List<StationDTO>
)

@Serializable
data class StationDTO(
    val country: String,
    val id: StationID,
    val names: StationNames,
    val location: LocationDTO
)

@Serializable
data class StationID(
    val code: String
)

@Serializable
data class StationNames(
    val long: String,
    val medium: String,
    val short: String
)

@Serializable
data class LocationDTO(
    val lat: Double,
    val lng: Double,
)