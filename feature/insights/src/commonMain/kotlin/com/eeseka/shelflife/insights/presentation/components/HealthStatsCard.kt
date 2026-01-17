package com.eeseka.shelflife.insights.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.insights.generated.resources.Res
import shelflife.feature.insights.generated.resources.a_healthy
import shelflife.feature.insights.generated.resources.diet_quantity
import shelflife.feature.insights.generated.resources.e_limit
import shelflife.feature.insights.generated.resources.eco_friendly_items_wasted
import shelflife.feature.insights.generated.resources.health_info
import shelflife.feature.insights.generated.resources.ultra_processed_items_eaten

@Composable
fun HealthStatsCard(
    nutriScoreDistribution: Map<String, Int>,
    ultraProcessedCount: Int,
    wastedGoodEcoCount: Int
) {
    val colorA = Color(0xFF038141)
    val colorB = Color(0xFF85BB2F)
    val colorC = Color(0xFFFECB02)
    val colorD = Color(0xFFEE8100)
    val colorE = Color(0xFFE63E11)

    val nutriColors = mapOf(
        "a" to colorA, "b" to colorB, "c" to colorC, "d" to colorD, "e" to colorE
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.health_info),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (nutriScoreDistribution.isNotEmpty()) {
                Column {
                    Text(
                        text = stringResource(Res.string.diet_quantity),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    var animationPlayed by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        animationPlayed = true
                    }

                    val animationProgress by animateFloatAsState(
                        targetValue = if (animationPlayed) 1f else 0f,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "bar_fill"
                    )

                    // Stacked Bar Chart Container
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        val total = nutriScoreDistribution.values.sum().toFloat()
                        if (total > 0) {
                            listOf("a", "b", "c", "d", "e").forEach { score ->
                                val count = nutriScoreDistribution[score] ?: 0
                                if (count > 0) {
                                    val share = (count / total)
                                    val animatedWeight = share * animationProgress
                                    // Prevents "IllegalArgumentException: invalid weight; must be greater than zero"
                                    if (animatedWeight > 0.001f) {
                                        Box(
                                            modifier = Modifier
                                                .weight(animatedWeight)
                                                .fillMaxHeight()
                                                .background(nutriColors[score] ?: Color.Gray)
                                        )
                                    }
                                }
                            }
                            // We use max(0f) to be safe, though 1f - progress shouldn't naturally go below 0
                            if (animationProgress < 1f) {
                                val emptyWeight = 1f - animationProgress
                                if (emptyWeight > 0.001f) {
                                    Spacer(modifier = Modifier.weight(emptyWeight))
                                }
                            }
                        }
                    }

                    // Legend
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.a_healthy),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorA,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.e_limit),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorE,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- NOVA & ECO SUMMARY ---
            // Only show if there is data to warn about
            if (ultraProcessedCount > 0 || wastedGoodEcoCount > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (ultraProcessedCount > 0) {
                        HealthSummaryRow(
                            icon = Icons.Default.Factory,
                            value = "$ultraProcessedCount",
                            label = stringResource(Res.string.ultra_processed_items_eaten),
                            // NOVA 4 Color (Red)
                            color = colorE
                        )
                    }

                    if (wastedGoodEcoCount > 0) {
                        HealthSummaryRow(
                            icon = Icons.Default.Eco,
                            value = "$wastedGoodEcoCount",
                            label = stringResource(Res.string.eco_friendly_items_wasted),
                            // Eco A Color (Green) - Using green here ironically to show what was LOST
                            color = colorA
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthSummaryRow(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
private fun HealthStatsCardPreview() {
    ShelfLifeTheme {
        HealthStatsCard(
            nutriScoreDistribution = mapOf("a" to 12, "b" to 5, "c" to 8, "d" to 2, "e" to 6),
            ultraProcessedCount = 4,
            wastedGoodEcoCount = 2
        )
    }
}