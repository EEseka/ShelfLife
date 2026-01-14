package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.util.clean
import com.eeseka.shelflife.pantry.presentation.util.getStorageIcon
import com.eeseka.shelflife.pantry.presentation.util.getStorageName
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.opened_date
import shelflife.feature.pantry.generated.resources.packaging_size
import shelflife.feature.pantry.generated.resources.purchase_date
import shelflife.feature.pantry.generated.resources.quantity
import shelflife.feature.pantry.generated.resources.storage_location

@Composable
fun DetailStatsGrid(item: PantryItem) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailStatCard(
                label = stringResource(Res.string.quantity),
                value = "${item.quantity.clean()} ${item.quantityUnit}",
                icon = Icons.Outlined.ShoppingBag,
                modifier = Modifier.weight(1f)
            )
            DetailStatCard(
                label = stringResource(Res.string.storage_location),
                value = getStorageName(item.storageLocation),
                icon = getStorageIcon(item.storageLocation),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailStatCard(
                label = stringResource(Res.string.purchase_date),
                value = item.purchaseDate.toString(),
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.weight(1f)
            )

            val openDate = item.openDate
            val packaging = item.packagingSize

            if (openDate != null) {
                DetailStatCard(
                    label = stringResource(Res.string.opened_date),
                    value = openDate.toString(),
                    icon = Icons.Outlined.LockOpen,
                    modifier = Modifier.weight(1f)
                )
            } else if (packaging != null) {
                DetailStatCard(
                    label = stringResource(Res.string.packaging_size),
                    value = packaging,
                    icon = Icons.Outlined.Scale,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailStatsGridPreview() {
    ShelfLifeTheme {
        DetailStatsGrid(previewPantryItem)
    }
}