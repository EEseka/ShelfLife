package com.eeseka.shelflife.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eeseka.shelflife.shared.data.settings.createDataStore
import com.eeseka.shelflife.shared.data.util.ConnectivityObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    single<DataStore<Preferences>> {
        createDataStore()
    }
    singleOf(::ConnectivityObserver)
}