package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.brand_unknown

@Composable
fun DetailHeader(
    itemName: String,
    itemBrand: String?
) {
    Column {
        Text(
            text = itemName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = itemBrand ?: stringResource(Res.string.brand_unknown),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailHeaderPreview() {
    ShelfLifeTheme {
        DetailHeader(
            itemName = "Bananas",
            itemBrand = "Fruitopia"
        )
    }
}