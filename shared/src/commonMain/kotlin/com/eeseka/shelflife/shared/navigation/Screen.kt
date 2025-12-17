package com.eeseka.shelflife.shared.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object HomeGraph : Screen

    @Serializable
    data object Pantry : Screen

    @Serializable
    data object Insights : Screen

    @Serializable
    data object Settings : Screen
}