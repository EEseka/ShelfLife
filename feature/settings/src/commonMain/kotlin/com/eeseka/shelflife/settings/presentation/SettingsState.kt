package com.eeseka.shelflife.settings.presentation

import androidx.compose.runtime.Immutable
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.settings.AppTheme
import com.eeseka.shelflife.shared.domain.settings.NotificationPreferences

@Immutable
data class SettingsState(
    val user: User? = null,
    val theme: AppTheme = AppTheme.SYSTEM,
    val notification: NotificationPreferences = NotificationPreferences(),
    val isConnectedToInternet: Boolean = false,
    val isSigningOut: Boolean = false,
    val isDeletingAccount: Boolean = false
)
