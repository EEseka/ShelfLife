package com.eeseka.shelflife

import android.app.Application
import com.eeseka.shelflife.di.initializeKoin
import org.koin.android.ext.koin.androidContext

class ShelfLifeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeKoin(config = {
            androidContext(this@ShelfLifeApplication)
        })
    }
}
