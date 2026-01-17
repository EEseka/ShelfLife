package com.eeseka.shelflife.insights.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.eeseka.shelflife.insights.presentation.util.clean
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.insight.InsightStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun InsightItemCard(item: InsightItem) {
    val isConsumed = item.status == InsightStatus.CONSUMED
    val statusColor =
        if (isConsumed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val statusIcon = if (isConsumed) Icons.Rounded.CheckCircle else Icons.Default.Delete

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(56.dp)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    placeholder = rememberVectorPainter(Icons.Outlined.Image),
                    error = rememberVectorPainter(Icons.Outlined.ImageNotSupported),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val currentYear =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

                val monthName = item.actionDate.month.name.take(3).lowercase()
                    .replaceFirstChar { it.uppercase() }

                val dateString = if (item.actionDate.year == currentYear) {
                    "$monthName ${item.actionDate.day}" // "Jan 12"
                } else {
                    "$monthName ${item.actionDate.day}, ${item.actionDate.year}" // "Jan 12, 2025"
                }

                val qtyString = "${item.quantity.clean()} ${item.quantityUnit}"

                Text(
                    text = "$dateString • $qtyString",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(statusColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
private fun InsightItemCardPreview() {
    ShelfLifeTheme {
        InsightItemCard(
            InsightItem(
                id = "1",
                name = "Bananas",
                imageUrl = null,
                quantity = 1.5,
                quantityUnit = "kg",
                actionDate = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date,
                status = InsightStatus.WASTED,
                nutriScore = "A",
                ecoScore = "A",
                novaGroup = 1
            )
        )
    }
}