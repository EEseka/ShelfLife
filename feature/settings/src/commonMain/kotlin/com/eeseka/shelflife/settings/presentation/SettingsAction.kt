package com.eeseka.shelflife.settings.presentation

import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.settings.AppTheme
import com.eeseka.shelflife.shared.presentation.permissions.PermissionState
import kotlinx.datetime.LocalTime

sealed interface SettingsAction {
    data class OnGoogleSignInSuccess(val user: User.Authenticated?) : SettingsAction
    data class OnGoogleSignInFailure(val error: Throwable) : SettingsAction
    data class OnChangeAppTheme(val theme: AppTheme) : SettingsAction
    data object OnToggleNotification : SettingsAction
    data class OnNotificationPermissionResult(val state: PermissionState) : SettingsAction
    data class OnSetNotificationTime(val time: LocalTime) : SettingsAction
    data object OnSignOutClicked : SettingsAction
    data object OnDeleteAccountClicked : SettingsAction
}