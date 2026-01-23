package com.eeseka.shelflife.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.domain.settings.NotificationPreferences
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import com.eeseka.shelflife.shared.presentation.permissions.PermissionState
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsService: SettingsService
) : ViewModel() {

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.OnGetStartedClick -> onGetStartedClick(action.permissionState)
        }
    }

    private fun onGetStartedClick(permissionState: PermissionState) {
        viewModelScope.launch {
            val allowed = permissionState == PermissionState.GRANTED

            settingsService.setNotificationPreferences(
                NotificationPreferences(allowed = allowed)
            )
            settingsService.setOnboardingCompleted()
        }
    }
}