package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.util.clean
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.calories_unit
import shelflife.feature.pantry.generated.resources.grams_unit
import shelflife.feature.pantry.generated.resources.nutrition
import shelflife.feature.pantry.generated.resources.nutrition_calories
import shelflife.feature.pantry.generated.resources.nutrition_fat
import shelflife.feature.pantry.generated.resources.nutrition_protein
import shelflife.feature.pantry.generated.resources.nutrition_sugar

@Composable
fun NutritionInfoPanel(
    caloriesPer100g: Int?,
    sugarPer100g: Double?,
    fatPer100g: Double?,
    proteinPer100g: Double?,
    modifier: Modifier = Modifier
) {
    val nutritionData = buildList {
        caloriesPer100g?.let {
            add(stringResource(Res.string.nutrition_calories) to "$it ${stringResource(Res.string.calories_unit)}")
        }
        sugarPer100g?.let {
            add(stringResource(Res.string.nutrition_sugar) to "${it.clean()} ${stringResource(Res.string.grams_unit)}")
        }
        fatPer100g?.let {
            add(stringResource(Res.string.nutrition_fat) to "${it.clean()} ${stringResource(Res.string.grams_unit)}")
        }
        proteinPer100g?.let {
            add(stringResource(Res.string.nutrition_protein) to "${it.clean()} ${stringResource(Res.string.grams_unit)}")
        }
    }

    if (nutritionData.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.nutrition),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                nutritionData.forEachIndexed { index, (label, value) ->
                    NutritionRow(label = label, value = value)

                    if (index < nutritionData.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NutritionInfoPanelPreview() {
    ShelfLifeTheme {
        NutritionInfoPanel(
            caloriesPer100g = 100,
            sugarPer100g = 1.0,
            fatPer100g = 1.0,
            proteinPer100g = 1.0
        )
    }
}