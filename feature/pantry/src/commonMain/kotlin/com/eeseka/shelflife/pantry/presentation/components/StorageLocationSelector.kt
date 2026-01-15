package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.freezer
import shelflife.feature.pantry.generated.resources.fridge
import shelflife.feature.pantry.generated.resources.pantry
import shelflife.feature.pantry.generated.resources.storage_location

@Composable
fun StorageLocationSelector(
    currentLocation: StorageLocation,
    onLocationSelected: (StorageLocation) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.storage_location),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StorageLocation.entries.forEach { location ->
                FilterChip(
                    selected = currentLocation == location,
                    onClick = {
                        if (currentLocation != location) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            onLocationSelected(location)
                        }
                    },
                    label = {
                        Text(
                            when (location) {
                                StorageLocation.PANTRY -> stringResource(Res.string.pantry)
                                StorageLocation.FRIDGE -> stringResource(Res.string.fridge)
                                StorageLocation.FREEZER -> stringResource(Res.string.freezer)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StorageLocationSelectorPreview() {
    ShelfLifeTheme {
        StorageLocationSelector(
            currentLocation = StorageLocation.PANTRY,
            onLocationSelected = { }
        )
    }
}