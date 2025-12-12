package com.eeseka.shelflife.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.domain.settings.SettingsRepository
import com.eeseka.shelflife.shared.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())

    // Combine local state with the 3 streams from DataStore
    val state = combine(
        _state,
        settingsRepository.theme,
        settingsRepository.hasSeenOnboarding,
        settingsRepository.cachedUser
    ) { state, theme, hasSeenOnboarding, user ->

        val destination = when {
            !hasSeenOnboarding -> Screen.Onboarding
            user != null -> Screen.HomeGraph
            else -> Screen.Auth
        }

        state.copy(
            isCheckingAuth = false,
            theme = theme,
            startDestination = destination
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MainState()
        )

    // In MainViewModel's init block or onStart
//    fun startMonitoringAuth() {
//        // This runs in the background FOREVER while the app is open
//        firebaseAuth.authState.collect { firebaseUser ->
//            if (firebaseUser == null && settingsRepository.cachedUser.value != null) {
//                // ALARM! Cache thinks we are logged in, but Firebase says NO.
//                // Kick them out.
//                settingsRepository.saveUser(null)
//                // The state.combine logic automatically sees 'null' and switches screen to Auth
//            }
//        }
//    }
}