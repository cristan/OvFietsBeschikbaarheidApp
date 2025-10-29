package nl.ovfietsbeschikbaarheid.mapper

import androidx.datastore.core.DataStore
import nl.ovfietsbeschikbaarheid.di.commonModule
import nl.ovfietsbeschikbaarheid.util.AndroidLocationLoader
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import nl.ovfietsbeschikbaarheid.util.InAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckModulesTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `check all modules`() {
        commonModule().verify(
            // These classes come from the androidModule
            extraTypes =
                listOf(
                    DataStore::class,
                    InAppReviewProvider::class,
                    LocationPermissionHelper::class,
                    AndroidLocationLoader::class,
                    LocationLoader::class,
                    DecimalFormatter::class
                )
        )
    }
}