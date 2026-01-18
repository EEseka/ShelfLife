package com.eeseka.shelflife.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.auth.presentation.components.AuthActions
import com.eeseka.shelflife.auth.presentation.components.AuthBranding
import com.eeseka.shelflife.shared.design_system.components.ShelfLifeScaffold
import com.eeseka.shelflife.shared.presentation.util.DeviceConfiguration
import com.eeseka.shelflife.shared.presentation.util.ObserveAsEvents
import com.eeseka.shelflife.shared.presentation.util.ShelfLifeSnackbarVisuals
import com.eeseka.shelflife.shared.presentation.util.SnackbarType
import com.eeseka.shelflife.shared.presentation.util.currentDeviceConfiguration
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import shelflife.feature.auth.generated.resources.Res
import shelflife.feature.auth.generated.resources.auth_disclosure

private const val ANIMATION_GROCERY_BAG = "grocery_delivery.json"

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AuthScreen(
    onAction: (AuthAction) -> Unit,
    events: Flow<AuthEvent>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var isGuestSigningIn by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current
    val privacyUrl = "https://www.google.com"

    val config = currentDeviceConfiguration()

    val brandingSize = when (config) {
        DeviceConfiguration.TABLET_PORTRAIT -> 400.dp
        else -> 250.dp
    }

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/${ANIMATION_GROCERY_BAG}").decodeToString()
        )
    }

    ObserveAsEvents(events) { event ->
        when (event) {
            is AuthEvent.Error -> {
                isGoogleSigningIn = false
                isGuestSigningIn = false
                val message = event.message.asStringAsync()
                val visuals = ShelfLifeSnackbarVisuals(
                    message = message,
                    type = SnackbarType.Error,
                    withDismissAction = true
                )
                snackbarHostState.showSnackbar(visuals)
            }

            is AuthEvent.Success -> {
                val message = event.message.asStringAsync()
                val visuals = ShelfLifeSnackbarVisuals(
                    message = message,
                    type = SnackbarType.Success,
                    withDismissAction = true
                )
                snackbarHostState.showSnackbar(visuals)
            }
        }
    }

    ShelfLifeScaffold(
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            when (config) {
                DeviceConfiguration.MOBILE_PORTRAIT,
                DeviceConfiguration.TABLET_PORTRAIT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Push Branding to visual center (or slightly higher)
                        Spacer(modifier = Modifier.weight(1f))

                        AuthBranding(
                            composition = composition,
                            iconSize = brandingSize
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        AuthActions(
                            isGoogleSigningIn = isGoogleSigningIn,
                            isGuestSigningIn = isGuestSigningIn,
                            enableButtons = !isGoogleSigningIn && !isGuestSigningIn,
                            onGoogleSignInSuccess = {
                                onAction(AuthAction.OnGoogleSignInSuccess(it))
                            },
                            onGoogleSignInFailure = {
                                onAction(AuthAction.OnGoogleSignInFailure(it))
                            },
                            onGoogleClick = { isGoogleSigningIn = true },
                            onGuestClick = {
                                isGuestSigningIn = true
                                onAction(AuthAction.OnGuestClick)
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = stringResource(Res.string.auth_disclosure),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.padding(bottom = 24.dp)
                                .clickable {
                                    uriHandler.openUri(privacyUrl)
                                }
                        )
                    }
                }

                else -> {
                    // --- LANDSCAPE / DESKTOP (Split Row) ---
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AuthBranding(
                                    composition = composition,
                                    iconSize = brandingSize
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                AuthActions(
                                    isGoogleSigningIn = isGoogleSigningIn,
                                    isGuestSigningIn = isGuestSigningIn,
                                    enableButtons = !isGoogleSigningIn && !isGuestSigningIn,
                                    onGoogleSignInSuccess = {
                                        onAction(AuthAction.OnGoogleSignInSuccess(it))
                                    },
                                    onGoogleSignInFailure = {
                                        onAction(AuthAction.OnGoogleSignInFailure(it))
                                    },
                                    onGoogleClick = { isGoogleSigningIn = true },
                                    onGuestClick = {
                                        isGuestSigningIn = true
                                        onAction(AuthAction.OnGuestClick)
                                    }
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = stringResource(Res.string.auth_disclosure),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri(privacyUrl)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}