package nl.ovfietsbeschikbaarheid.di

import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.mobile
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.repository.VehiclesRepository
import nl.ovfietsbeschikbaarheid.usecase.FindNearbyLocationsUseCase
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import nl.ovfietsbeschikbaarheid.viewmodel.HomeViewModel
import nl.ovfietsbeschikbaarheid.viewmodel.MapViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule() = module {
    factoryOf(::KtorApiClient)
    factory { Geocoder.mobile() }

    singleOf(::OverviewRepository)
    singleOf(::StationRepository)
    singleOf(::VehiclesRepository)
    singleOf(::LocationPermissionHelper)
    factoryOf(::LocationLoader)
    factoryOf(::FindNearbyLocationsUseCase)

    viewModelOf(::DetailsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MapViewModel)
}