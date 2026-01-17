package com.eeseka.shelflife.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.eeseka.shelflife.shared.data.database.local.DatabaseFactory
import com.eeseka.shelflife.shared.data.export.FileExporter
import com.eeseka.shelflife.shared.data.media.ImageCompressor
import com.eeseka.shelflife.shared.data.notification.NotificationScheduler
import com.eeseka.shelflife.shared.data.settings.createDataStore
import com.eeseka.shelflife.shared.data.util.ConnectivityObserver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule = module {
    single<DataStore<Preferences>> { createDataStore(androidContext()) }
    single<HttpClientEngine> { OkHttp.create() }

    singleOf(::DatabaseFactory)
    singleOf(::ConnectivityObserver)
    singleOf(::NotificationScheduler)
    singleOf(::ImageCompressor)
    singleOf(::FileExporter)
}