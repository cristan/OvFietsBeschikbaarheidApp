package nl.ovfietsbeschikbaarheid.di

import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import nl.ovfietsbeschikbaarheid.viewmodel.HomeViewModel
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.mobile
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.mobile
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule() = module {
    factoryOf(::KtorApiClient)
    factory { Geolocator.mobile() }
    factory { Geocoder.mobile() }

    singleOf(::OverviewRepository)
    singleOf(::StationRepository)
    singleOf(::LocationPermissionHelper)

    viewModelOf(::DetailsViewModel)
    viewModelOf(::HomeViewModel)
}