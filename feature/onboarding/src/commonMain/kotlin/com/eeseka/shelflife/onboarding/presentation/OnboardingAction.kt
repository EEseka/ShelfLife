package com.eeseka.shelflife.onboarding.presentation

sealed interface OnboardingAction {
    data object OnGetStartedClick : OnboardingAction
}