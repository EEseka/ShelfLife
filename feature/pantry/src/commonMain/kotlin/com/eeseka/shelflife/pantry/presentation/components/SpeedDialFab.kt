package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.add_item
import shelflife.feature.pantry.generated.resources.enter_manually
import shelflife.feature.pantry.generated.resources.scan_product

@Composable
fun SpeedDialFab(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onScan: () -> Unit,
    onManual: () -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Scrim (Dim background when open)
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                    onDismiss()
                }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {

        // Mini FAB 1: Manual Entry
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically { it } + fadeIn() + scaleIn(),
            exit = slideOutVertically { it } + fadeOut() + scaleOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.enter_manually),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                SmallFloatingActionButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        onManual()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Edit, stringResource(Res.string.enter_manually))
                }
            }
        }

        // Mini FAB 2: Scan
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically { it } + fadeIn() + scaleIn(),
            exit = slideOutVertically { it } + fadeOut() + scaleOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.scan_product),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                SmallFloatingActionButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        onScan()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.QrCodeScanner, stringResource(Res.string.scan_product))
                }
            }
        }

        // Main Toggle FAB
        val rotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f)

        FloatingActionButton(
            onClick = {
                hapticFeedback.performHapticFeedback(
                    if (isExpanded) HapticFeedbackType.ToggleOff
                    else HapticFeedbackType.ToggleOn
                )
                onToggle()
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.add_item),
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpeedDialFabPreview() {
    ShelfLifeTheme {
        SpeedDialFab(
            isExpanded = false,
            onToggle = {},
            onScan = {},
            onManual = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SpeedDialFabPreviewDark() {
    ShelfLifeTheme(true) {
        SpeedDialFab(
            isExpanded = false,
            onToggle = {},
            onScan = {},
            onManual = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SpeedDialFabExpandedPreview() {
    ShelfLifeTheme {
        SpeedDialFab(
            isExpanded = true,
            onToggle = {},
            onScan = {},
            onManual = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SpeedDialFabExpandedPreviewDark() {
    ShelfLifeTheme(true) {
        SpeedDialFab(
            isExpanded = true,
            onToggle = {},
            onScan = {},
            onManual = {},
            onDismiss = {}
        )
    }
}