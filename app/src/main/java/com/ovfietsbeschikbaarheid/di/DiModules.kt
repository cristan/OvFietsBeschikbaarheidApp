package com.ovfietsbeschikbaarheid.di

import com.ovfietsbeschikbaarheid.repository.OverviewRepository
import com.ovfietsbeschikbaarheid.repository.StationRepository
import com.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import com.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import com.ovfietsbeschikbaarheid.viewmodel.LocationsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule() = module {
    singleOf(::OverviewRepository)
    singleOf(::StationRepository)
    singleOf(::LocationPermissionHelper)

    viewModelOf(::DetailsViewModel)
    viewModelOf(::LocationsViewModel)
}