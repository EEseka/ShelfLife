package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eeseka.shelflife.pantry.presentation.util.calculateExpiryState
import com.eeseka.shelflife.pantry.presentation.util.clean
import com.eeseka.shelflife.pantry.presentation.util.getStorageIcon
import com.eeseka.shelflife.pantry.presentation.util.getStorageName
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.brand_unknown

@Composable
fun PantryItemCard(
    item: PantryItem,
    onClick: () -> Unit
) {
    val expiryState = calculateExpiryState(item.expiryDate)

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            Box(contentAlignment = Alignment.Center) {
                val model = item.thumbnailUrl ?: item.imageUrl

                AsyncImage(
                    model = model,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Outlined.Image),
                    error = rememberVectorPainter(Icons.Outlined.ImageNotSupported),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    ExpiryChip(state = expiryState)
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = item.brand ?: stringResource(Res.string.brand_unknown),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Location Badge
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = getStorageIcon(item.storageLocation),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = getStorageName(item.storageLocation),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Quantity
                    Text(
                        text = "${item.quantity.clean()} ${item.quantityUnit}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PantryItemCardPreview() {
    ShelfLifeTheme {
        PantryItemCard(
            item = previewPantryItem,
            onClick = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PantryItemCardPreviewDark() {
    ShelfLifeTheme(true) {
        PantryItemCard(
            item = previewPantryItem,
            onClick = { }
        )
    }
}

internal val previewPantryItem = PantryItem(
    id = "preview_uuid_123",
    barcode = "123456789",
    name = "Organic Creamy Peanut Butter",
    brand = "Whole Earth",
    // Use a null image for preview so it falls back to your placeholder icon
    imageUrl = null,
    thumbnailUrl = null,

    quantity = 6.0,
    quantityUnit = "jar",
    packagingSize = "454g",

    // Dates (using kotlinx.datetime.LocalDate)
    expiryDate = LocalDate(2026, 12, 25), // Future date
    purchaseDate = LocalDate(2026, 1, 15), // Recent past
    openDate = LocalDate(2026, 1, 15),

    storageLocation = StorageLocation.PANTRY,

    // Scores for UI testing
    nutriScore = "c", // Should probably be yellow in your UI
    novaGroup = 3,    // Processed
    ecoScore = "b",   // Low impact

    // Lists for chips/badges
    allergens = listOf("en:peanuts"),
    labels = listOf("en:organic", "en:vegan", "en:gluten-free"),

    // Macros
    caloriesPer100g = 590,
    sugarPer100g = 3.1,
    fatPer100g = 49.0,
    proteinPer100g = 25.0
)