package com.eeseka.shelflife

import androidx.compose.ui.window.ComposeUIViewController
import com.eeseka.shelflife.di.initializeKoin

fun MainViewController() = ComposeUIViewController(configure = { initializeKoin() }) { App() }