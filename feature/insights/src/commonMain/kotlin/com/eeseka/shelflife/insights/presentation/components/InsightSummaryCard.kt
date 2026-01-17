package com.eeseka.shelflife.insights.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.insights.generated.resources.Res
import shelflife.feature.insights.generated.resources.consumed
import shelflife.feature.insights.generated.resources.wasted

@Composable
fun InsightSummaryCard(
    consumedPercentage: Float,
    consumedCount: Int,
    wastedCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DonutChart(
                    percentage = consumedPercentage,
                    radius = 60.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Text Stats
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatItem(
                        count = consumedCount,
                        label = stringResource(Res.string.consumed),
                        color = MaterialTheme.colorScheme.primary,
                        icon = Icons.Rounded.CheckCircle
                    )
                    StatItem(
                        count = wastedCount,
                        label = stringResource(Res.string.wasted),
                        color = MaterialTheme.colorScheme.error,
                        icon = Icons.Default.Delete
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightSummaryCardPreview() {
    ShelfLifeTheme {
        InsightSummaryCard(
            consumedPercentage = 0.75f,
            consumedCount = 12,
            wastedCount = 3
        )
    }
}