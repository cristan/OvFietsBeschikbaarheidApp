package nl.ovfietsbeschikbaarheid.repository

import kotlinx.coroutines.test.runTest
import nl.ovfietsbeschikbaarheid.testutils.locationsResponse
import nl.ovfietsbeschikbaarheid.testutils.shouldBeEqualTo
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocationsCacheRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val cacheDirectory by lazy { temporaryFolder.root.toOkioPath() }

    @Test
    fun `there are no cached locations before any are stored`() = runTest {
        LocationsCacheRepository(cacheDirectory).getLocations() shouldBeEqualTo null
    }

    @Test
    fun `stored locations are still there after an app restart`() = runTest {
        LocationsCacheRepository(cacheDirectory).store(locationsResponse)

        LocationsCacheRepository(cacheDirectory).getLocations() shouldBeEqualTo locationsResponse
    }

    @Test
    fun `partially stored locations are ignored`() = runTest {
        LocationsCacheRepository(cacheDirectory).store(locationsResponse)
        val cacheFile = FileSystem.SYSTEM.list(cacheDirectory).single()
        val storedJson = FileSystem.SYSTEM.read(cacheFile) { readUtf8() }
        FileSystem.SYSTEM.write(cacheFile) { writeUtf8(storedJson.substring(0, storedJson.length / 2)) }

        LocationsCacheRepository(cacheDirectory).getLocations() shouldBeEqualTo null
    }
}
