package com.eeseka.shelflife.di

import com.eeseka.shelflife.onboarding.presentation.OnboardingViewModel
import com.eeseka.shelflife.shared.data.logging.KermitLogger
import com.eeseka.shelflife.shared.data.settings.DataStoreSettingsRepository
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.settings.SettingsRepository
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
    singleOf(::DataStoreSettingsRepository) bind SettingsRepository::class

    // ComposeApp module
    viewModelOf(::MainViewModel)

    //Feature: Onboarding module
    viewModelOf(::OnboardingViewModel)
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