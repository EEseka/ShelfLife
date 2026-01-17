package com.eeseka.shelflife.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.eeseka.shelflife.auth.presentation.AuthViewModel
import com.eeseka.shelflife.insights.data.OfflineFirstInsightRepository
import com.eeseka.shelflife.insights.domain.InsightRepository
import com.eeseka.shelflife.insights.presentation.InsightViewModel
import com.eeseka.shelflife.onboarding.presentation.OnboardingViewModel
import com.eeseka.shelflife.pantry.data.OfflineFirstPantryRepository
import com.eeseka.shelflife.pantry.domain.PantryRepository
import com.eeseka.shelflife.pantry.presentation.form.PantryFormViewModel
import com.eeseka.shelflife.pantry.presentation.pantry_list_detail.PantryViewModel
import com.eeseka.shelflife.settings.presentation.SettingsViewModel
import com.eeseka.shelflife.shared.data.auth.FirebaseAuthService
import com.eeseka.shelflife.shared.data.database.local.DatabaseFactory
import com.eeseka.shelflife.shared.data.database.local.RoomLocalInsightStorageService
import com.eeseka.shelflife.shared.data.database.local.RoomLocalPantryStorageService
import com.eeseka.shelflife.shared.data.database.remote.FirebaseFirestoreRemoteInsightStorageService
import com.eeseka.shelflife.shared.data.database.remote.FirebaseFirestoreRemotePantryStorageService
import com.eeseka.shelflife.shared.data.export.NativeFileExportService
import com.eeseka.shelflife.shared.data.logging.KermitLogger
import com.eeseka.shelflife.shared.data.media.NativeImageCompressionService
import com.eeseka.shelflife.shared.data.networking.HttpClientFactory
import com.eeseka.shelflife.shared.data.networking.KtorApiService
import com.eeseka.shelflife.shared.data.notification.NativeNotificationService
import com.eeseka.shelflife.shared.data.settings.DataStoreSettingsService
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.database.local.LocalInsightStorageService
import com.eeseka.shelflife.shared.domain.database.local.LocalPantryStorageService
import com.eeseka.shelflife.shared.domain.database.remote.RemoteInsightStorageService
import com.eeseka.shelflife.shared.domain.database.remote.RemotePantryStorageService
import com.eeseka.shelflife.shared.domain.export.FileExportService
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.media.ImageCompressionService
import com.eeseka.shelflife.shared.domain.networking.ApiService
import com.eeseka.shelflife.shared.domain.notification.NotificationService
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import com.eeseka.shelflife.shared.presentation.MainViewModel
import com.eeseka.shelflife.shared.presentation.util.ScopedStoreRegistryViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    // Shared module
    single<ShelfLifeLogger> { KermitLogger }
    single {
        HttpClientFactory(get()).create(get())
    }
    singleOf(::DataStoreSettingsService) bind SettingsService::class
    singleOf(::FirebaseAuthService) bind AuthService::class
    singleOf(::FirebaseFirestoreRemotePantryStorageService) bind RemotePantryStorageService::class
    singleOf(::FirebaseFirestoreRemoteInsightStorageService) bind RemoteInsightStorageService::class
    singleOf(::RoomLocalPantryStorageService) bind LocalPantryStorageService::class
    singleOf(::RoomLocalInsightStorageService) bind LocalInsightStorageService::class
    singleOf(::NativeNotificationService) bind NotificationService::class
    singleOf(::NativeImageCompressionService) bind ImageCompressionService::class
    singleOf(::NativeFileExportService) bind FileExportService::class
    singleOf(::KtorApiService) bind ApiService::class
    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    viewModelOf(::ScopedStoreRegistryViewModel)

    // ComposeApp module
    viewModelOf(::MainViewModel)

    // Feature: Onboarding module
    viewModelOf(::OnboardingViewModel)

    // Feature: Auth module
    viewModelOf(::AuthViewModel)

    // Feature: Settings module
    viewModelOf(::SettingsViewModel)

    // Feature: Pantry module
    singleOf(::OfflineFirstPantryRepository) bind PantryRepository::class
    viewModelOf(::PantryViewModel)
    viewModelOf(::PantryFormViewModel)

    // Feature: Insights module
    singleOf(::OfflineFirstInsightRepository) bind InsightRepository::class
    viewModelOf(::InsightViewModel)
}

expect val platformModule: Module

fun initializeKoin(
    config: (KoinApplication.() -> Unit)? = null
) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule, platformModule)
    }
}