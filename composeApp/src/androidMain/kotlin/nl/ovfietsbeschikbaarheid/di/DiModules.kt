package nl.ovfietsbeschikbaarheid.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
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
import nl.ovfietsbeschikbaarheid.util.AndroidInAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.DecimalFormatter
import nl.ovfietsbeschikbaarheid.util.InAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.LocationLoader
import nl.ovfietsbeschikbaarheid.util.LocationPermissionHelper
import nl.ovfietsbeschikbaarheid.util.RatingEligibilityService
import nl.ovfietsbeschikbaarheid.viewmodel.DetailsViewModel
import nl.ovfietsbeschikbaarheid.viewmodel.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule() = module {
    factoryOf(::KtorApiClient)
    factory { Geocoder.mobile() }

    singleOf(::RatingStorageRepository)
    singleOf(::RatingEligibilityService)
    singleOf(::OverviewRepository)
    singleOf(::StationRepository)
    singleOf(::DetailsRepository)
    factoryOf(::DecimalFormatter)
    factoryOf(::DetailsMapper)
    factoryOf(::LocationsMapper)
    factoryOf(::FindNearbyLocationsUseCase)

    viewModelOf(::DetailsViewModel)
    viewModelOf(::HomeViewModel)
}

val Context.dataStore by preferencesDataStore("settings")

fun androidModule() = module {
    single { androidContext().dataStore }
    single<AndroidInAppReviewProvider> { AndroidInAppReviewProvider(get()) }
    single<InAppReviewProvider> { get<AndroidInAppReviewProvider>() }
    singleOf(::LocationPermissionHelper)
    factoryOf(::LocationLoader)
}