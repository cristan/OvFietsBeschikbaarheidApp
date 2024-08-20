package com.ovfietsbeschikbaarheid

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import com.ovfietsbeschikbaarheid.repository.StationRepository
import com.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import com.ovfietsbeschikbaarheid.viewmodel.LocationsViewModel

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setUpKoin()
    }

    private fun setUpKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(
                module {
                    singleOf(::OverviewRepository)
                    singleOf(::StationRepository)

                    viewModelOf(::DetailsViewModel)
                    viewModelOf(::LocationsViewModel)
                }
            )
        }
    }
}
