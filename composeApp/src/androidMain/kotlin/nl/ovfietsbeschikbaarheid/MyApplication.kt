package nl.ovfietsbeschikbaarheid

import android.app.Application
import nl.ovfietsbeschikbaarheid.di.appModule
import nl.ovfietsbeschikbaarheid.ext.createActivityLifecycleObserver
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setUpKoin()

        val locationPermissionHelper: LocationPermissionHelper by inject()

        registerActivityLifecycleCallbacks(createActivityLifecycleObserver { activity ->
            locationPermissionHelper.setActivity(activity)
        })
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
