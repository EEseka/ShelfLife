package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.eco_score
import shelflife.feature.pantry.generated.resources.health_info
import shelflife.feature.pantry.generated.resources.nova
import shelflife.feature.pantry.generated.resources.nutri_score

@Composable
fun HealthScoresSection(nutriScore: String?, ecoScore: String?, novaGroup: Int?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(Res.string.health_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            nutriScore?.let {
                HealthScoreBadge(
                    type = stringResource(Res.string.nutri_score),
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
            ecoScore?.let {
                HealthScoreBadge(
                    type = stringResource(Res.string.eco_score),
                    value = it,
                    modifier = Modifier.weight(1f)
                )
            }
            novaGroup?.let {
                HealthScoreBadge(
                    type = stringResource(Res.string.nova),
                    value = "$it",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthScoresSectionPreview() {
    ShelfLifeTheme {
        HealthScoresSection(
            nutriScore = "a",
            ecoScore = "b",
            novaGroup = 1
        )
    }
}