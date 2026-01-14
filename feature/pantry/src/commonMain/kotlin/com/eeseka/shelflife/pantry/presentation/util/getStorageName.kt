package com.eeseka.shelflife.pantry.presentation.util

import androidx.compose.runtime.Composable
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import org.jetbrains.compose.resources.stringResource
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.freezer
import shelflife.feature.pantry.generated.resources.fridge
import shelflife.feature.pantry.generated.resources.pantry

@Composable
fun getStorageName(location: StorageLocation) = when (location) {
    StorageLocation.PANTRY -> stringResource(Res.string.pantry)
    StorageLocation.FRIDGE -> stringResource(Res.string.fridge)
    StorageLocation.FREEZER -> stringResource(Res.string.freezer)
}