package nl.ovfietsbeschikbaarheid

import android.app.Application
import nl.ovfietsbeschikbaarheid.di.appModule
import nl.ovfietsbeschikbaarheid.ext.createActivityLifecycleObserver
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setUpTimber()
        setUpKoin()

        val locationPermissionHelper: LocationPermissionHelper by inject()

        registerActivityLifecycleCallbacks(createActivityLifecycleObserver { activity ->
            locationPermissionHelper.setActivity(activity)
        })
    }

    private fun setUpTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun setUpKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(
                appModule()
            )
        }
    }
}
