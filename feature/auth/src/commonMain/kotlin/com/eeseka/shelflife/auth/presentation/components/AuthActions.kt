package com.eeseka.shelflife.auth.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.auth.User
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.auth.generated.resources.Res
import shelflife.feature.auth.generated.resources.continue_as_guest
import shelflife.feature.auth.generated.resources.continue_with_google
import shelflife.feature.auth.generated.resources.google_logo

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AuthActions(
    isGoogleSigningIn: Boolean,
    isGuestSigningIn: Boolean,
    enableButtons: Boolean,
    onGoogleSignInSuccess: (user: User.Authenticated?) -> Unit,
    onGoogleSignInFailure: (error: Throwable) -> Unit,
    onGoogleClick: () -> Unit,
    onGuestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GoogleButtonUiContainerFirebase(
            linkAccount = false,
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
                    onGoogleSignInSuccess(user)
                }.onFailure { error ->
                    onGoogleSignInFailure(error)
                }
            }
        ) {
            SocialAuthButton(
                text = stringResource(Res.string.continue_with_google),
                icon = painterResource(Res.drawable.google_logo),
                isLoading = isGoogleSigningIn,
                enabled = enableButtons,
                onClick = {
                    onGoogleClick()
                    this@GoogleButtonUiContainerFirebase.onClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OrDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(
            onClick = onGuestClick,
            enabled = enableButtons,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            AnimatedContent(targetState = isGuestSigningIn) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.continue_as_guest),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AuthActionsPreview() {
    ShelfLifeTheme {
        AuthActions(
            isGoogleSigningIn = false,
            isGuestSigningIn = false,
            enableButtons = true,
            onGoogleSignInSuccess = { },
            onGoogleSignInFailure = { },
            onGoogleClick = { },
            onGuestClick = { }
        )
    }
}