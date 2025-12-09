package com.eeseka.shelflife.di

import com.eeseka.shelflife.shared.data.logging.KermitLogger
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule = module {
    // Shared module
    single<ShelfLifeLogger> { KermitLogger }
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