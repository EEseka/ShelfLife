package com.eeseka.shelflife.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import com.eeseka.shelflife.shared.navigation.Screen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authService: AuthService,
    private val settingsRepository: SettingsService
) : ViewModel() {

    init {
        authService.authState
            .onEach { firebaseUser ->
                settingsRepository.saveUser(firebaseUser)
            }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            authService.validateSession()
        }
    }

    val state = combine(
        settingsRepository.cachedUser,
        settingsRepository.theme,
        settingsRepository.hasSeenOnboarding
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