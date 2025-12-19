package com.eeseka.shelflife.di

import com.eeseka.shelflife.auth.presentation.AuthViewModel
import com.eeseka.shelflife.onboarding.presentation.OnboardingViewModel
import com.eeseka.shelflife.settings.presentation.SettingsViewModel
import com.eeseka.shelflife.shared.data.auth.FirebaseAuthService
import com.eeseka.shelflife.shared.data.logging.KermitLogger
import com.eeseka.shelflife.shared.data.settings.DataStoreSettingsService
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import com.eeseka.shelflife.shared.presentation.MainViewModel
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
    singleOf(::DataStoreSettingsService) bind SettingsService::class
    singleOf(::FirebaseAuthService) bind AuthService::class

    // ComposeApp module
    viewModelOf(::MainViewModel)

    // Feature: Onboarding module
    viewModelOf(::OnboardingViewModel)

    // Feature: Auth module
    viewModelOf(::AuthViewModel)

    // Feature: Settings module
    viewModelOf(::SettingsViewModel)
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