package com.eeseka.shelflife

import android.app.Application
import com.eeseka.shelflife.di.initializeKoin
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.koin.android.ext.koin.androidContext

class ShelfLifeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        // Initialize Google Auth
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(serverId = AppConfig.WEB_CLIENT_ID)
        )

        // Initialize Koin
        initializeKoin(config = {
            androidContext(this@ShelfLifeApplication)
        })
    }
}