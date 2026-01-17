package com.eeseka.shelflife.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.settings.presentation.components.AuthenticatedProfileCard
import com.eeseka.shelflife.settings.presentation.components.ExportDataItem
import com.eeseka.shelflife.settings.presentation.components.GuestModeCard
import com.eeseka.shelflife.settings.presentation.components.NotificationItem
import com.eeseka.shelflife.settings.presentation.components.SettingsFooter
import com.eeseka.shelflife.settings.presentation.components.SettingsSection
import com.eeseka.shelflife.settings.presentation.components.SyncStatusItem
import com.eeseka.shelflife.settings.presentation.components.ThemeSelectionItem
import com.eeseka.shelflife.shared.design_system.components.ShelfLifeScaffold
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.presentation.permissions.Permission
import com.eeseka.shelflife.shared.presentation.permissions.PermissionState
import com.eeseka.shelflife.shared.presentation.permissions.rememberPermissionController
import com.eeseka.shelflife.shared.presentation.util.DeviceConfiguration
import com.eeseka.shelflife.shared.presentation.util.ObserveAsEvents
import com.eeseka.shelflife.shared.presentation.util.ShelfLifeSnackbarVisuals
import com.eeseka.shelflife.shared.presentation.util.SnackbarType
import com.eeseka.shelflife.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import shelflife.feature.settings.generated.resources.Res
import shelflife.feature.settings.generated.resources.cancel
import shelflife.feature.settings.generated.resources.data
import shelflife.feature.settings.generated.resources.delete
import shelflife.feature.settings.generated.resources.delete_account_question
import shelflife.feature.settings.generated.resources.delete_account_with_question_mark
import shelflife.feature.settings.generated.resources.general
import shelflife.feature.settings.generated.resources.notification_permission_required
import shelflife.feature.settings.generated.resources.ok
import shelflife.feature.settings.generated.resources.open_settings
import shelflife.feature.settings.generated.resources.reset
import shelflife.feature.settings.generated.resources.reset_app_question
import shelflife.feature.settings.generated.resources.reset_app_with_question_mark
import shelflife.feature.settings.generated.resources.settings
import shelflife.feature.settings.generated.resources.sign_out
import shelflife.feature.settings.generated.resources.sign_out_question
import shelflife.feature.settings.generated.resources.sign_out_with_question_mark
import shelflife.feature.settings.generated.resources.to_continue_please_grant_permission_in_app_settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    events: Flow<SettingsEvent>,
    onAction: (SettingsAction) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionController = rememberPermissionController()
    val config = currentDeviceConfiguration()
    val hapticFeedback = LocalHapticFeedback.current

    var isSignInWithGoogleLoading by remember { mutableStateOf(false) }

    // Dialog States
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmation by remember { mutableStateOf(false) }

    ObserveAsEvents(events) { event ->
        when (event) {
            is SettingsEvent.Success -> {
                isSignInWithGoogleLoading = false
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        event.message.asStringAsync(),
                        SnackbarType.Success
                    )
                )
            }

            is SettingsEvent.Error -> {
                isSignInWithGoogleLoading = false
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        event.message.asStringAsync(),
                        SnackbarType.Error
                    )
                )
            }

            SettingsEvent.CheckAndRequestNotificationPermission -> {
                val currentState = permissionController.getPermissionState(Permission.NOTIFICATIONS)
                if (currentState == PermissionState.GRANTED) {
                    onAction(SettingsAction.OnNotificationPermissionResult(PermissionState.GRANTED))
                } else {
                    val result = permissionController.requestPermission(Permission.NOTIFICATIONS)
                    onAction(SettingsAction.OnNotificationPermissionResult(result))
                }
            }

            SettingsEvent.OpenAppSettings -> {
                showPermissionRationaleDialog = true
            }
        }
    }

    // Identity Content (Just the profile card)
    val identityContent: @Composable () -> Unit = {
        if (state.user is User.Authenticated) {
            AuthenticatedProfileCard(user = state.user)
        } else {
            GuestModeCard(
                isLinkingAccountLoading = isSignInWithGoogleLoading,
                onGoogleClick = { isSignInWithGoogleLoading = true },
                onGoogleSignInSuccess = { onAction(SettingsAction.OnGoogleSignInSuccess(it)) },
                onGoogleSignInFailure = { onAction(SettingsAction.OnGoogleSignInFailure(it)) },
            )
        }
    }

    // Preferences Content (General + Data)
    val preferencesContent: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            SettingsSection(title = stringResource(Res.string.general)) {
                ThemeSelectionItem(
                    currentTheme = state.theme,
                    onThemeSelected = { onAction(SettingsAction.OnChangeAppTheme(it)) }
                )
                SettingsDivider()
                NotificationItem(
                    allowed = state.notification.allowed,
                    time = state.notification.reminderTime,
                    onToggle = { onAction(SettingsAction.OnToggleNotification) },
                    onTimeClick = { if (state.notification.allowed) showTimePicker = true }
                )
            }

            SettingsSection(title = stringResource(Res.string.data)) {
                ExportDataItem(
                    onExportClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        onAction(SettingsAction.ExportPantryData)
                    }
                )
                SettingsDivider()
                SyncStatusItem(
                    isGuest = state.user !is User.Authenticated,
                    isConnected = state.isConnectedToInternet
                )
            }
        }
    }

    // Footer Actions
    val footerContent: @Composable () -> Unit = {
        SettingsFooter(
            isGuest = state.user !is User.Authenticated,
            isSigningOut = state.isSigningOut,
            isDeleting = state.isDeletingAccount,
            onSignOut = { showLogoutConfirmation = true },
            onDelete = { showDeleteAccountConfirmation = true }
        )
    }

    ShelfLifeScaffold(
        snackbarHostState = snackbarHostState,
        topBar = { SettingsTopBar() },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            when (config) {
                DeviceConfiguration.MOBILE_PORTRAIT,
                DeviceConfiguration.TABLET_PORTRAIT -> {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        identityContent()
                        preferencesContent()

                        Spacer(modifier = Modifier.weight(1f))

                        footerContent()

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 400.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            identityContent()
                            Spacer(modifier = Modifier.weight(1f))
                            footerContent()
                        }

                        Column(
                            modifier = Modifier
                                .weight(1.5f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            preferencesContent()
                        }
                    }
                }
            }
        }

        // --- Dialogs ---
        if (showPermissionRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionRationaleDialog = false },
                title = { Text(stringResource(Res.string.notification_permission_required)) },
                text = { Text(stringResource(Res.string.to_continue_please_grant_permission_in_app_settings)) },
                confirmButton = {
                    Button(onClick = {
                        permissionController.openAppSettings()
                        showPermissionRationaleDialog = false
                    }) { Text(stringResource(Res.string.open_settings)) }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionRationaleDialog = false }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }

        if (showTimePicker) {
            val timeState = rememberTimePickerState(
                initialHour = state.notification.reminderTime.hour,
                initialMinute = state.notification.reminderTime.minute,
                is24Hour = false
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    Button(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        onAction(
                            SettingsAction.OnSetNotificationTime(
                                LocalTime(
                                    timeState.hour,
                                    timeState.minute
                                )
                            )
                        )
                        showTimePicker = false
                    }) { Text(stringResource(Res.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        showTimePicker = false
                    }) { Text(stringResource(Res.string.cancel)) }
                },
                text = { TimePicker(state = timeState) }
            )
        }

        if (showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirmation = false },
                title = { Text(stringResource(Res.string.sign_out_with_question_mark)) },
                text = { Text(stringResource(Res.string.sign_out_question)) },
                confirmButton = {
                    Button(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        showLogoutConfirmation = false
                        onAction(SettingsAction.OnSignOutClicked)
                    }) { Text(stringResource(Res.string.sign_out)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        showLogoutConfirmation = false
                    }) { Text(stringResource(Res.string.cancel)) }
                }
            )
        }

        if (showDeleteAccountConfirmation) {
            val isGuest = state.user !is User.Authenticated
            AlertDialog(
                onDismissRequest = { showDeleteAccountConfirmation = false },
                title = {
                    Text(
                        if (isGuest) stringResource(Res.string.reset_app_with_question_mark)
                        else stringResource(Res.string.delete_account_with_question_mark)
                    )
                },
                text = {
                    Text(
                        if (isGuest) stringResource(Res.string.reset_app_question)
                        else stringResource(Res.string.delete_account_question)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                            onAction(SettingsAction.OnDeleteAccountClicked)
                            showDeleteAccountConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text(if (isGuest) stringResource(Res.string.reset) else stringResource(Res.string.delete)) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        showDeleteAccountConfirmation = false
                    }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.settings),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}