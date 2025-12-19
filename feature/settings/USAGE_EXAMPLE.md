# Settings Feature - Usage Guide

## Architecture Overview

This Settings feature follows the **Chirp-style MVI/MVVM hybrid pattern**:

- **Actions**: Flow UP from Screen → ViewModel (user intents)
- **State**: Single source of truth, observed by UI
- **Events**: One-time side effects, flow DOWN from ViewModel → Screen

## Implementation Example

Here's how to implement the Settings screen composable:

```kotlin
@Composable
fun SettingsRoot(
    viewModel: SettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionController = rememberPermissionController()
    
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.Success -> {
                // Show success toast/snackbar
            }
            is SettingsEvent.Error -> {
                // Show error toast/snackbar
            }
            SettingsEvent.CheckAndRequestNotificationPermission -> {
                // IMPORTANT: Check permission state FIRST before requesting
                val currentState = permissionController.getPermissionState(Permission.NOTIFICATIONS)
                
                if (currentState == PermissionState.GRANTED) {
                    // Already granted, just enable notifications
                    viewModel.onAction(SettingsAction.OnNotificationPermissionResult(PermissionState.GRANTED))
                } else {
                    // Need to request permission
                    val result = permissionController.requestPermission(Permission.NOTIFICATIONS)
                    viewModel.onAction(SettingsAction.OnNotificationPermissionResult(result))
                }
            }
            SettingsEvent.OpenAppSettings -> {
                // Show dialog to guide user to app settings
                // You can use a state variable to show/hide the dialog
            }
        }
    }
    
    SettingsScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit
) {
    Column {
        // Theme Section
        ThemeSelector(
            selectedTheme = state.theme,
            onThemeSelected = { theme ->
                onAction(SettingsAction.OnChangeAppTheme(theme))
            }
        )
        
        // Notifications Section
        SwitchRow(
            title = "Notifications",
            checked = state.notification.allowed,
            onCheckedChange = {
                onAction(SettingsAction.OnToggleNotification)
            }
        )
        
        // If notifications are enabled, show time picker
        if (state.notification.allowed) {
            TimePickerRow(
                selectedTime = state.notification.reminderTime,
                onTimeSelected = { time ->
                    onAction(SettingsAction.OnSetNotificationTime(time))
                }
            )
        }
        
        // Google Sign-In (for Guest users only)
        if (state.user is User.Guest) {
            GoogleButtonUiContainerFirebase(
                linkAccount = true,  // Important: Set to true for account linking
                onResult = { result ->
                    result.onSuccess { firebaseUser ->
                        val user = firebaseUser?.let { user ->
                            User.Authenticated(
                                id = user.uid,
                                email = user.email ?: "",
                                fullName = user.displayName ?: "",
                                profilePictureUrl = user.photoURL
                            )
                        }
                        onAction(SettingsAction.OnGoogleSignInSuccess(user))
                    }.onFailure { error ->
                        onAction(SettingsAction.OnGoogleSignInFailure(error))
                    }
                }
            ) {
                // Your Google button UI
            }
        }
        
        // Sign Out Button
        Button(
            onClick = { onAction(SettingsAction.OnSignOutClicked) },
            enabled = !state.isSigningOut
        ) {
            if (state.isSigningOut) {
                CircularProgressIndicator()
            } else {
                Text("Sign Out")
            }
        }
        
        // Delete Account Button
        Button(
            onClick = { onAction(SettingsAction.OnDeleteAccountClicked) },
            enabled = !state.isDeletingAccount,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            if (state.isDeletingAccount) {
                CircularProgressIndicator()
            } else {
                Text("Delete Account")
            }
        }
    }
}
```

## Notification Permission Flow

### Step-by-Step:

1. **User toggles notification ON**
   - ViewModel sends `RequestNotificationPermission` event
   
2. **Composable receives event**
   - Uses `permissionController.requestPermission(Permission.NOTIFICATIONS)`
   - Sends result back via `OnNotificationPermissionResult` action
   
3. **ViewModel handles result**
   - `GRANTED`: Saves preference as `true`
   - `DENIED`: Keeps preference as `false`
   - `PERMANENTLY_DENIED`: Sends `OpenAppSettings` event

### Handling Permanently Denied:

```kotlin
var showSettingsDialog by remember { mutableStateOf(false) }

ObserveAsEvents(viewModel.events) { event ->
    when (event) {
        SettingsEvent.OpenAppSettings -> {
            showSettingsDialog = true
        }
        // ... other events
    }
}

if (showSettingsDialog) {
    AlertDialog(
        onDismissRequest = { showSettingsDialog = false },
        title = { Text("Notifications Permission Required") },
        text = { 
            Text("To enable notifications, please grant permission in app settings.") 
        },
        confirmButton = {
            Button(
                onClick = {
                    // Open app settings
                    // Platform-specific implementation needed
                    showSettingsDialog = false
                }
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            Button(onClick = { showSettingsDialog = false }) {
                Text("Cancel")
            }
        }
    )
}
```

## Key Points

1. **Don't inject PermissionController** - Create it in the composable with `rememberPermissionController()`
2. **Use Events for permission requests** - Keeps permission logic in the UI layer where it belongs
3. **State flows DOWN, Actions flow UP** - This is the Chirp pattern
4. **Loading states are managed** - `isSigningOut`, `isDeletingAccount` for proper UX
5. **Connectivity is observed** - `isConnectedToInternet` available in state

## Testing

The ViewModel is fully testable because:
- No Android dependencies
- Permissions are handled via events (can be tested separately)
- All dependencies are injected
- State changes are predictable

```kotlin
class SettingsViewModelTest {
    private val settingsService = FakeSettingsService()
    private val authService = FakeAuthService()
    private lateinit var viewModel: SettingsViewModel
    
    @Test
    fun `toggle notifications sends permission request event`() = runTest {
        viewModel = SettingsViewModel(settingsService, authService, ...)
        
        viewModel.onAction(SettingsAction.OnToggleNotification)
        
        val event = viewModel.events.first()
        assertIs<SettingsEvent.RequestNotificationPermission>(event)
    }
}
```

## Notes

- Remember to implement notification scheduling when `allowed = true` and `reminderTime` is set
- Firebase auth state syncs automatically via MainViewModel after Google sign-in
- Account deletion requires manual cleanup in Firebase Console
