package nl.ovfietsbeschikbaarheid.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.mobile
import kotlinx.cinterop.ExperimentalForeignApi
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import nl.ovfietsbeschikbaarheid.util.IOSLocationLoader
import nl.ovfietsbeschikbaarheid.util.IOSPlatformLocationHelper
import nl.ovfietsbeschikbaarheid.util.InAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.IosInAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import nl.ovfietsbeschikbaarheid.util.PlatformLocationHelper
import okio.Path.Companion.toPath
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

fun iosModule() = module {
    single { createIosDataStore() }
    single<Locator> { Locator.mobile() }
    single<InAppReviewProvider> { IosInAppReviewProvider() }
    factoryOf<PlatformLocationHelper>(::IOSPlatformLocationHelper)
    singleOf(::LocationPermissionHelper)
    factory<LocationLoader> { IOSLocationLoader(get()) }
    factoryOf(::DecimalFormatter)
}

@OptIn(ExperimentalForeignApi::class)
fun createIosDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            {
                val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
                requireNotNull(documentDirectory).path + "/settings.preferences_pb"
            }().toPath()
        }
    )