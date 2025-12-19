package com.eeseka.shelflife.settings.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.settings.generated.resources.Res
import shelflife.feature.settings.generated.resources.clear_all_data_and_reset
import shelflife.feature.settings.generated.resources.delete_account
import shelflife.feature.settings.generated.resources.deleting_account
import shelflife.feature.settings.generated.resources.resetting
import shelflife.feature.settings.generated.resources.sign_out
import shelflife.feature.settings.generated.resources.signing_out

@Composable
fun SettingsFooter(
    isGuest: Boolean,
    isSigningOut: Boolean,
    isDeleting: Boolean,
    onSignOut: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isGuest) {
            OutlinedButton(
                onClick = onSignOut,
                enabled = !isSigningOut,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                ButtonContent(
                    isLoading = isSigningOut,
                    loadingText = stringResource(Res.string.signing_out),
                    idleText = stringResource(Res.string.sign_out),
                    idleIcon = Icons.AutoMirrored.Filled.Logout
                )
            }

            Button(
                onClick = onDelete,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                ButtonContent(
                    isLoading = isDeleting,
                    loadingText = stringResource(Res.string.deleting_account),
                    idleText = stringResource(Res.string.delete_account),
                    idleIcon = Icons.Filled.Delete
                )
            }
        } else {
            TextButton(
                onClick = onDelete,
                enabled = !isDeleting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                ButtonContent(
                    isLoading = isDeleting,
                    loadingText = stringResource(Res.string.resetting),
                    idleText = stringResource(Res.string.clear_all_data_and_reset),
                    idleIcon = Icons.Filled.ClearAll
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    isLoading: Boolean,
    loadingText: String,
    idleText: String,
    idleIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedContent(targetState = isLoading) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
            } else {
                Icon(
                    imageVector = idleIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = if (isLoading) loadingText else idleText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsFooterPreview() {
    ShelfLifeTheme {
        SettingsFooter(
            isGuest = false,
            isSigningOut = false,
            isDeleting = false,
            onSignOut = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsFooterGuestPreview() {
    ShelfLifeTheme {
        SettingsFooter(
            isGuest = true,
            isSigningOut = false,
            isDeleting = false,
            onSignOut = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsFooterSigningOutPreview() {
    ShelfLifeTheme {
        SettingsFooter(
            isGuest = false,
            isSigningOut = true,
            isDeleting = false,
            onSignOut = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsFooterDeletingPreview() {
    ShelfLifeTheme {
        SettingsFooter(
            isGuest = false,
            isSigningOut = false,
            isDeleting = true,
            onSignOut = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsFooterGuestDeletingPreview() {
    ShelfLifeTheme {
        SettingsFooter(
            isGuest = true,
            isSigningOut = false,
            isDeleting = true,
            onSignOut = {},
            onDelete = {}
        )
    }
}