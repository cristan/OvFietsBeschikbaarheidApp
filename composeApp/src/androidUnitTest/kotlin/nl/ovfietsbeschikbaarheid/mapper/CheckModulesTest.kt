package nl.ovfietsbeschikbaarheid.mapper

import androidx.datastore.core.DataStore
import nl.ovfietsbeschikbaarheid.di.appModule
import nl.ovfietsbeschikbaarheid.util.AndroidLocationLoader
import nl.ovfietsbeschikbaarheid.util.InAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckModulesTest : KoinTest {

    @Test
    fun `check all modules`() {
        // These classes come from the androidModule
        appModule().verify(extraTypes =
            listOf(DataStore::class, InAppReviewProvider::class, LocationPermissionHelper::class, AndroidLocationLoader::class, LocationLoader::class)
        )
    }
}