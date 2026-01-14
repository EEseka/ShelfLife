package com.eeseka.shelflife.pantry.presentation.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Kitchen
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation

fun getStorageIcon(location: StorageLocation) = when (location) {
    StorageLocation.PANTRY -> Icons.Default.Inventory2
    StorageLocation.FRIDGE -> Icons.Filled.Kitchen
    StorageLocation.FREEZER -> Icons.Default.AcUnit
}
