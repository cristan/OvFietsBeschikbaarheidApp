package com.ovfietsbeschikbaarheid.di

import com.ovfietsbeschikbaarheid.KtorApiClient
import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import com.ovfietsbeschikbaarheid.repository.StationRepository
import com.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import com.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import com.ovfietsbeschikbaarheid.viewmodel.LocationsViewModel
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.mobile
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule() = module {
    factoryOf(::KtorApiClient)
    factory { Geolocator.mobile() }

    singleOf(::OverviewRepository)
    singleOf(::StationRepository)
    singleOf(::LocationPermissionHelper)

    viewModelOf(::DetailsViewModel)
    viewModelOf(::LocationsViewModel)
}