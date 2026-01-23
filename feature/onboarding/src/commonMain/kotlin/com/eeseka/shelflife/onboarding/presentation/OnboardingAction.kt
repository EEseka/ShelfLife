package com.eeseka.shelflife.onboarding.presentation

import com.eeseka.shelflife.shared.presentation.permissions.PermissionState

sealed interface OnboardingAction {
    data class OnGetStartedClick(val permissionState: PermissionState) : OnboardingAction
}