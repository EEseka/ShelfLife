package com.eeseka.shelflife.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.data.util.ConnectivityObserver
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.settings.AppTheme
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import com.eeseka.shelflife.shared.domain.util.onFailure
import com.eeseka.shelflife.shared.domain.util.onSuccess
import com.eeseka.shelflife.shared.presentation.permissions.PermissionState
import com.eeseka.shelflife.shared.presentation.util.UiText
import com.eeseka.shelflife.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import shelflife.feature.settings.generated.resources.Res
import shelflife.feature.settings.generated.resources.account_disabled
import shelflife.feature.settings.generated.resources.auth_success
import shelflife.feature.settings.generated.resources.internet_connection_unavailable
import shelflife.feature.settings.generated.resources.sign_in_canceled
import shelflife.feature.settings.generated.resources.unknown_error_occurred

class SettingsViewModel(
    private val settingsService: SettingsService,
    private val authService: AuthService,
    private val connectivityObserver: ConnectivityObserver,
    private val shelfLifeLogger: ShelfLifeLogger
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = combine(
        _state,
        settingsService.cachedUser,
        settingsService.theme,
        settingsService.notificationPreferences,
        connectivityObserver.isConnected
    ) { currentState, cachedUser, theme, notification, isConnectedToInternet ->
        currentState.copy(
            user = cachedUser,
            theme = theme,
            notification = notification,
            isConnectedToInternet = isConnectedToInternet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsState()
    )
    private val eventChannel = Channel<SettingsEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnChangeAppTheme -> changeAppTheme(action.theme)
            SettingsAction.OnDeleteAccountClicked -> deleteAccount()
            is SettingsAction.OnGoogleSignInFailure -> handleGoogleError(action.error)
            is SettingsAction.OnGoogleSignInSuccess -> handleGoogleSignInToUpgradeGuestSuccess(
                action.user
            )

            is SettingsAction.OnSetNotificationTime -> changeNotificationTime(action.time)
            SettingsAction.OnSignOutClicked -> signOut()
            SettingsAction.OnToggleNotification -> toggleNotifications()
            is SettingsAction.OnNotificationPermissionResult -> handlePermissionResult(action.state)
        }
    }

    private fun changeAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsService.setTheme(theme)
        }
    }

    private fun changeNotificationTime(time: LocalTime) {
        viewModelScope.launch {
            settingsService.setNotificationPreferences(
                state.value.notification.copy(reminderTime = time)
            )
        }
    }

    private fun toggleNotifications() {
        val currentPreference = state.value.notification.allowed

        if (currentPreference) {
            // CASE 2: User wants to turn OFF
            // We DON'T revoke system permission (we can't).
            viewModelScope.launch {
                settingsService.setNotificationPreferences(
                    state.value.notification.copy(allowed = false)
                )
                // TODO: Cancel daily alarm when notification manager is implemented
                // notificationScheduler.cancelDailyAlarm()
            }
        } else {
            // CASE 1: User wants to turn ON
            // Send event to check permission state first, then request if needed
            viewModelScope.launch {
                eventChannel.send(SettingsEvent.CheckAndRequestNotificationPermission)
            }
        }
    }

    private fun handlePermissionResult(permissionState: PermissionState) {
        viewModelScope.launch {
            when (permissionState) {
                PermissionState.GRANTED -> {
                    // User granted permission, update our app preference
                    settingsService.setNotificationPreferences(
                        state.value.notification.copy(allowed = true)
                    )
                    // TODO: Schedule daily alarm at notification.reminderTime when notification manager is implemented
                    // notificationScheduler.scheduleDailyAlarm(state.value.notification.reminderTime)
                }

                PermissionState.DENIED -> {
                    // User denied this time, keep toggle OFF
                    settingsService.setNotificationPreferences(
                        state.value.notification.copy(allowed = false)
                    )
                }

                PermissionState.PERMANENTLY_DENIED -> {
                    // User permanently denied, guide them to settings
                    settingsService.setNotificationPreferences(
                        state.value.notification.copy(allowed = false)
                    )
                    eventChannel.send(SettingsEvent.OpenAppSettings)
                }

                PermissionState.NOT_DETERMINED -> {
                    // Should not happen in this flow, but handle gracefully
                    settingsService.setNotificationPreferences(
                        state.value.notification.copy(allowed = false)
                    )
                }
            }
        }
    }

    private fun handleGoogleSignInToUpgradeGuestSuccess(user: User.Authenticated?) {
        viewModelScope.launch {
            if (user != null) {
                authService.reloadAndGetUpgradedUser()
                    .onSuccess { user ->
                        settingsService.saveUser(user)
                        eventChannel.send(SettingsEvent.Success(UiText.Resource(Res.string.auth_success)))
                    }
                    .onFailure { error ->
                        // Handle rare edge case where reload fails
                        eventChannel.send(SettingsEvent.Error(error.toUiText()))
                    }
            } else {
                shelfLifeLogger.warn("Google sign in succeeded but user is null")
                eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.unknown_error_occurred)))
            }
        }
    }

    private fun handleGoogleError(error: Throwable) {
        viewModelScope.launch {
            if (error.message?.contains("A network error") == true) {
                eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.internet_connection_unavailable)))
            } else if (error.message?.contains("Idtoken is null") == true) {
                eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.sign_in_canceled)))
            } else if (error.message?.contains("The user account has been disabled") == true) {
                eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.account_disabled)))
            } else if (error.message?.contains("This credential is already associated with a different user") == true) {
                eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.account_disabled)))
            } else {
                shelfLifeLogger.error(error.message ?: "Unknown error", error.cause)
                eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.unknown_error_occurred)))
            }
        }
    }

    private fun signOut() {
        _state.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            authService.signOut()
                .onSuccess {
                    _state.update { it.copy(isSigningOut = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSigningOut = false) }
                    eventChannel.send(SettingsEvent.Error(error.toUiText()))
                }
        }
    }

    private fun deleteAccount() {
        _state.update { it.copy(isDeletingAccount = true) }
        viewModelScope.launch {
            authService.deleteAccount()
                .onSuccess {
                    _state.update { it.copy(isDeletingAccount = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isDeletingAccount = false) }
                    eventChannel.send(SettingsEvent.Error(error.toUiText()))
                }
        }
    } // Dont forget to clean up in firebase console
}