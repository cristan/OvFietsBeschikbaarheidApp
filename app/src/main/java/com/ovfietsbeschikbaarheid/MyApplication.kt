package com.ovfietsbeschikbaarheid

import android.app.Application
import com.ovfietsbeschikbaarheid.ext.createActivityLifecycleObserver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import com.ovfietsbeschikbaarheid.repository.StationRepository
import com.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import com.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import com.ovfietsbeschikbaarheid.viewmodel.LocationsViewModel
import org.koin.android.ext.android.inject

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
                module {
                    singleOf(::OverviewRepository)
                    singleOf(::StationRepository)
                    singleOf(::LocationPermissionHelper)

                    viewModelOf(::DetailsViewModel)
                    viewModelOf(::LocationsViewModel)
                }
            )
        }
    }
}
