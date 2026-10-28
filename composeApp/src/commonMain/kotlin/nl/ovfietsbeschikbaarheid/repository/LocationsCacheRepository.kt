package nl.ovfietsbeschikbaarheid.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import nl.ovfietsbeschikbaarheid.dto.LocationDTO
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.SYSTEM

class LocationsCacheRepository(cacheDirectory: Path) {

    private val cacheFile = cacheDirectory / "locations.json"
    private val temporaryCacheFile = cacheDirectory / "locations.json.tmp"
    private val storeMutex = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun store(locations: List<LocationDTO>) {
        withContext(Dispatchers.IO) {
            storeMutex.withLock {
                try {
                    FileSystem.SYSTEM.write(temporaryCacheFile) {
                        writeUtf8(json.encodeToString(locations))
                    }
                    FileSystem.SYSTEM.atomicMove(temporaryCacheFile, cacheFile)
                } catch (e: IOException) {
                    Logger.e(e) { "Failed to cache the locations" }
                }
            }
        }
    }

    suspend fun getLocations(): List<LocationDTO>? = withContext(Dispatchers.IO) {
        val cachedJson = try {
            FileSystem.SYSTEM.read(cacheFile) { readUtf8() }
        } catch (_: FileNotFoundException) {
            return@withContext null
        }
        try {
            json.decodeFromString<List<LocationDTO>>(cachedJson)
        } catch (e: SerializationException) {
            Logger.w(e) { "Ignoring the cached locations: they can't be read" }
            null
        }
    }
}
