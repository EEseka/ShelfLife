package com.eeseka.shelflife.settings.presentation

import com.eeseka.shelflife.shared.presentation.util.UiText

sealed interface SettingsEvent {
    data class Success(val message: UiText) : SettingsEvent
    data class Error(val message: UiText) : SettingsEvent
    data object CheckAndRequestNotificationPermission : SettingsEvent
    data object OpenAppSettings : SettingsEvent
}