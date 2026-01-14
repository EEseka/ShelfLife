package com.eeseka.shelflife.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.database.local.LocalStorageService
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import com.eeseka.shelflife.shared.navigation.Screen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authService: AuthService,
    private val settingsService: SettingsService,
    private val localStorageService: LocalStorageService,
) : ViewModel() {

    init {
        authService.authState
            .onEach { firebaseUser ->
                // Get the OLD user ID from your DataStore/Settings (before we overwrite it)
                val oldUserId = settingsService.cachedUser.first()?.id
                val newUserId = firebaseUser?.id

                // The "User Switch" Logic
                // If we had a user, and now we have a DIFFERENT user (or null), wipe the DB.
                if (oldUserId != null && oldUserId != newUserId) {
                    localStorageService.deleteAllPantryItems()
                    settingsService.clearUserPreferences()
                }

                // Now save the new user to DataStore
                settingsService.saveUser(firebaseUser)
            }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            authService.validateSession()
        }
    }

    val state = combine(
        settingsService.cachedUser,
        settingsService.theme,
        settingsService.hasSeenOnboarding
    ) { cachedUser, theme, hasSeenOnboarding ->

        val destination = when {
            !hasSeenOnboarding -> Screen.Onboarding
            cachedUser != null -> Screen.HomeGraph
            else -> Screen.Auth
        }

        MainState(
            isCheckingAuth = false,
            theme = theme,
            startDestination = destination
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MainState()
    )
}