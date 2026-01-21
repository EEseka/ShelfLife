package com.eeseka.shelflife

import android.app.Application
import com.eeseka.shelflife.di.initializeKoin
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.koin.android.ext.koin.androidContext

class ShelfLifeApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        // Initialize App Check
        val appCheck = Firebase.appCheck
        appCheck.installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) {
                // DEVELOPER MODE: Generates a debug token in Logcat
                // You must copy this token to Firebase Console -> App Check -> Apps -> Android -> Debug Tokens
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

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