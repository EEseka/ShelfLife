package com.eeseka.shelflife.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsService
) : ViewModel() {

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.OnGetStartedClick -> onGetStartedClick()
        }
    }

    private fun onGetStartedClick() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
        }
    }
}