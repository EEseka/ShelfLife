package com.eeseka.shelflife.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.settings.generated.resources.Res
import shelflife.feature.settings.generated.resources.sync_active
import shelflife.feature.settings.generated.resources.sync_inactive
import shelflife.feature.settings.generated.resources.sync_paused
import shelflife.feature.settings.generated.resources.sync_status

@Composable
fun SyncStatusItem(isGuest: Boolean, isConnected: Boolean) {
    val statusText = when {
        isGuest -> stringResource(Res.string.sync_inactive)
        isConnected -> stringResource(Res.string.sync_active)
        else -> stringResource(Res.string.sync_paused)
    }

    val statusColor = if (isGuest || !isConnected)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val icon = if (isGuest || !isConnected) Icons.Outlined.CloudOff else Icons.Outlined.Sync

    val iconTint = if (isGuest || !isConnected)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onTertiaryContainer

    val iconBg = if (isGuest || !isConnected)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.tertiaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(Res.string.sync_status),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = if (isGuest) FontWeight.Bold else FontWeight.Normal
            )
            if (!isGuest && isConnected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncStatusItemPreview() {
    ShelfLifeTheme {
        SyncStatusItem(isGuest = false, isConnected = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncStatusItemNotConnectedPreview() {
    ShelfLifeTheme {
        SyncStatusItem(isGuest = false, isConnected = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncStatusItemGuestPreview() {
    ShelfLifeTheme {
        SyncStatusItem(isGuest = true, isConnected = true)
    }
}