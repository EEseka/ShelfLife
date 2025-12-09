package com.eeseka.shelflife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var shouldShowSplashScreen = true

        installSplashScreen().setKeepOnScreenCondition {
            shouldShowSplashScreen
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
            LaunchedEffect(Unit) {
                delay(2000L)
                shouldShowSplashScreen = false
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}