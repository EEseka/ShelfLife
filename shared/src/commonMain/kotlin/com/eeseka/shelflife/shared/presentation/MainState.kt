package com.eeseka.shelflife.shared.presentation

import androidx.compose.runtime.Immutable
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.settings.AppTheme
import com.eeseka.shelflife.shared.navigation.Screen

@Immutable
data class MainState(
    val isCheckingAuth: Boolean = true,
    val startDestination: Screen? = null,
    val theme: AppTheme = AppTheme.SYSTEM
)