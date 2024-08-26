package com.ovfietsbeschikbaarheid

import android.app.Application
import com.ovfietsbeschikbaarheid.di.appModule
import com.ovfietsbeschikbaarheid.ext.createActivityLifecycleObserver
import com.ovfietsbeschikbaarheid.util.LocationPermissionHelper
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
