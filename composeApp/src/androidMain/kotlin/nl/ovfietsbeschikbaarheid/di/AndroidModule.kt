package nl.ovfietsbeschikbaarheid.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import nl.ovfietsbeschikbaarheid.model.AppRelease
import nl.ovfietsbeschikbaarheid.util.AndroidInAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.AndroidLocationLoader
import nl.ovfietsbeschikbaarheid.util.AndroidPlatformLocationHelper
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import nl.ovfietsbeschikbaarheid.util.InAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import nl.ovfietsbeschikbaarheid.util.PlatformLocationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")

fun androidAppModule() = module {
    includes(
        commonModule(),
        androidOnlyModule()
    )
}

fun androidOnlyModule() = module {
    single { androidContext().dataStore }
    single {
        val context = androidContext()
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        AppRelease(platform = "android", versionName = requireNotNull(versionName))
    }
    single<AndroidInAppReviewProvider> { AndroidInAppReviewProvider(get()) }
    single<InAppReviewProvider> { get<AndroidInAppReviewProvider>() }
    single<AndroidPlatformLocationHelper> { AndroidPlatformLocationHelper(get()) }
    single<PlatformLocationHelper> { get<AndroidPlatformLocationHelper>() }
    singleOf(::LocationPermissionHelper)
    factory<LocationLoader> { AndroidLocationLoader(get()) }
    factoryOf(::DecimalFormatter)
}