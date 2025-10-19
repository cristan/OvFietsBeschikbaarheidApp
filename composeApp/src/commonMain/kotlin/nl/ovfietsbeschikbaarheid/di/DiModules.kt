package nl.ovfietsbeschikbaarheid.di

import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.mobile
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.mapper.DetailsMapper
import nl.ovfietsbeschikbaarheid.mapper.LocationsMapper
import nl.ovfietsbeschikbaarheid.repository.DetailsRepository
import nl.ovfietsbeschikbaarheid.repository.OverviewRepository
import nl.ovfietsbeschikbaarheid.repository.RatingStorageRepository
import nl.ovfietsbeschikbaarheid.repository.StationRepository
import nl.ovfietsbeschikbaarheid.usecase.FindNearbyLocationsUseCase
import nl.ovfietsbeschikbaarheid.util.RatingEligibilityService
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import nl.ovfietsbeschikbaarheid.viewmodel.HomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun commonModule() = module {
    factoryOf(::KtorApiClient)
    factory { Geocoder.mobile() }

    singleOf(::RatingStorageRepository)
    singleOf(::RatingEligibilityService)
    singleOf(::OverviewRepository)
    singleOf(::StationRepository)
    singleOf(::DetailsRepository)
    factoryOf(::DetailsMapper)
    factoryOf(::LocationsMapper)
    factoryOf(::FindNearbyLocationsUseCase)

    viewModelOf(::DetailsViewModel)
    viewModelOf(::HomeViewModel)
}