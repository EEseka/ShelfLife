package com.eeseka.shelflife.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.data.util.ConnectivityObserver
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.database.local.LocalPantryStorageService
import com.eeseka.shelflife.shared.domain.export.CsvGenerator
import com.eeseka.shelflife.shared.domain.export.FileExportService
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.notification.NotificationService
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import shelflife.feature.settings.generated.resources.Res
import shelflife.feature.settings.generated.resources.account_disabled
import shelflife.feature.settings.generated.resources.auth_success
import shelflife.feature.settings.generated.resources.export_failed
import shelflife.feature.settings.generated.resources.internet_connection_unavailable
import shelflife.feature.settings.generated.resources.no_items_to_export
import shelflife.feature.settings.generated.resources.sign_in_canceled
import shelflife.feature.settings.generated.resources.unknown_error_occurred
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SettingsViewModel(
    private val settingsService: SettingsService,
    private val authService: AuthService,
    private val connectivityObserver: ConnectivityObserver,
    private val shelfLifeLogger: ShelfLifeLogger,
    private val notificationService: NotificationService,
    private val fileExportService: FileExportService,
    private val localPantryStorageService: LocalPantryStorageService
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
    private val _eventChannel = Channel<SettingsEvent>()
    val events = _eventChannel.receiveAsFlow()

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
            SettingsAction.ExportPantryData -> exportPantryData()
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
            if (state.value.notification.allowed) {
                notificationService.scheduleDailyNotification(time)
            }
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
                notificationService.cancelDailyNotification()
            }
        } else {
            // CASE 1: User wants to turn ON
            // Send event to check permission state first, then request if needed
            viewModelScope.launch {
                _eventChannel.send(SettingsEvent.CheckAndRequestNotificationPermission)
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
                    notificationService.scheduleDailyNotification(state.value.notification.reminderTime)
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
                    _eventChannel.send(SettingsEvent.OpenAppSettings)
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
                        _eventChannel.send(SettingsEvent.Success(UiText.Resource(Res.string.auth_success)))
                    }
                    .onFailure { error ->
                        // Handle rare edge case where reload fails
                        _eventChannel.send(SettingsEvent.Error(error.toUiText()))
                    }
            } else {
                shelfLifeLogger.warn("Google sign in succeeded but user is null")
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.unknown_error_occurred)))
            }
        }
    }

    private fun handleGoogleError(error: Throwable) {
        viewModelScope.launch {
            if (error.message?.contains("A network error") == true) {
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.internet_connection_unavailable)))
            } else if (error.message?.contains("Idtoken is null") == true) {
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.sign_in_canceled)))
            } else if (error.message?.contains("The user account has been disabled") == true) {
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.account_disabled)))
            } else if (error.message?.contains("This credential is already associated with a different user") == true) {
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.account_disabled)))
            } else {
                shelfLifeLogger.error(error.message ?: "Unknown error", error.cause)
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.unknown_error_occurred)))
            }
        }
    }

    private fun signOut() {
        _state.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            // Cancel Alarms (Good hygiene)
            notificationService.cancelDailyNotification()
            authService.signOut()
                .onSuccess {
                    _state.update { it.copy(isSigningOut = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSigningOut = false) }
                    _eventChannel.send(SettingsEvent.Error(error.toUiText()))
                }
        }
    }

    private fun deleteAccount() {
        _state.update { it.copy(isDeletingAccount = true) }
        viewModelScope.launch {
            notificationService.cancelDailyNotification()
            authService.deleteAccount()
                .onSuccess {
                    _state.update { it.copy(isDeletingAccount = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isDeletingAccount = false) }
                    _eventChannel.send(SettingsEvent.Error(error.toUiText()))
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun exportPantryData() {
        viewModelScope.launch {
            val items = localPantryStorageService.getAllPantryItems().first()

            if (items.isEmpty()) {
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.no_items_to_export)))
                return@launch
            }

            val csvContent = CsvGenerator.generatePantryCsv(items)
            val fileName = "shelfLife_inventory_${Clock.System.now().toEpochMilliseconds()}.csv"

            val isExportComplete = fileExportService.exportFile(fileName, csvContent)

            if (!isExportComplete) {
                shelfLifeLogger.error("Export failed in Service")
                _eventChannel.send(SettingsEvent.Error(UiText.Resource(Res.string.export_failed)))
            }
        }
    }
}