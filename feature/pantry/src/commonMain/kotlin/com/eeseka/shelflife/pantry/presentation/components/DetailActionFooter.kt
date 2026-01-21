package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.consumed
import shelflife.feature.pantry.generated.resources.wasted

@Composable
fun DetailActionFooter(
    isLoading: Boolean,
    onConsumed: () -> Unit,
    onWasted: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                            onWasted()
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        if (!isLoading) {
                            Icon(Icons.Outlined.Delete, null)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = stringResource(Res.string.wasted),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Button(
                        enabled = !isLoading,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onConsumed()
                        },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        if (!isLoading) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = stringResource(Res.string.consumed),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailActionFooterPreview() {
    ShelfLifeTheme {
        DetailActionFooter(
            isLoading = false,
            onConsumed = {},
            onWasted = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailActionFooterPreviewLoading() {
    ShelfLifeTheme {
        DetailActionFooter(
            isLoading = true,
            onConsumed = {},
            onWasted = {}
        )
    }
}

