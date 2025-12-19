package com.eeseka.shelflife.settings.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.auth.User
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.settings.generated.resources.Res
import shelflife.feature.settings.generated.resources.google_logo
import shelflife.feature.settings.generated.resources.guest_mode
import shelflife.feature.settings.generated.resources.link
import shelflife.feature.settings.generated.resources.sign_in_to_sync_your_pantry

@Composable
fun AuthenticatedProfileCard(user: User.Authenticated, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePictureUrl != null) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val initials = remember(user.fullName, user.email) {
                        val nameInitials = user.fullName
                            .trim()
                            .split("\\s+".toRegex())
                            .filter { it.isNotEmpty() }
                            .take(2)
                            .joinToString("") { it.first().uppercase() }

                        nameInitials.ifEmpty { user.email.take(2).uppercase() }
                    }

                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GuestModeCard(
    isLinkingAccountLoading: Boolean,
    onGoogleClick: () -> Unit,
    onGoogleSignInSuccess: (user: User.Authenticated?) -> Unit,
    onGoogleSignInFailure: (error: Throwable) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.guest_mode),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(Res.string.sign_in_to_sync_your_pantry),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // --- KMP AUTH CONTAINER ---
            GoogleButtonUiContainerFirebase(
                linkAccount = true,
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
                Button(
                    onClick = {
                        onGoogleClick()
                        this@GoogleButtonUiContainerFirebase.onClick()
                    },
                    enabled = !isLinkingAccountLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    AnimatedContent(targetState = isLinkingAccountLoading) { loading ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    painter = painterResource(Res.drawable.google_logo),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.link),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun AuthenticatedProfileCardPreview() {
    ShelfLifeTheme {
        AuthenticatedProfileCard(
            user = User.Authenticated(
                id = "123",
                fullName = "Eseka Emmanuel",
                email = "william.henry.harrison@example-pet-store.com",
                profilePictureUrl = null
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthenticatedProfileCardWithPicturePreview() {
    ShelfLifeTheme {
        AuthenticatedProfileCard(
            user = User.Authenticated(
                id = "123",
                fullName = "Eseka Emmanuel",
                email = "william.henry.harrison@example-pet-store.com",
                profilePictureUrl = "https://example.com/image.png"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuestModeCardPreview() {
    ShelfLifeTheme {
        GuestModeCard(
            onGoogleClick = {},
            isLinkingAccountLoading = false,
            onGoogleSignInSuccess = { },
            onGoogleSignInFailure = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GuestModeCardLinkingPreview() {
    ShelfLifeTheme {
        GuestModeCard(
            onGoogleClick = {},
            isLinkingAccountLoading = true,
            onGoogleSignInSuccess = { },
            onGoogleSignInFailure = { },
        )
    }
}
