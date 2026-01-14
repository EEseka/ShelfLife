package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun HealthScoreBadge(
    type: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isUnknown = value.equals("unknown", ignoreCase = true)
    val displayValue = if (isUnknown) "—" else value.uppercase()

    val (backgroundColor, textColor) = when {
        // --- NOVA GROUPS (Numbers) ---
        type.equals("NOVA", ignoreCase = true) -> when (value) { // Maybe use the string resource here instead of "NOVA"
            "1" -> Color(0xFF038141) to Color.White
            "2" -> Color(0xFFFECB02) to Color.Black
            "3" -> Color(0xFFEE8100) to Color.White
            "4" -> Color(0xFFE63E11) to Color.White
            else -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
        }

        // --- NUTRI & ECO SCORE (Letters) ---
        else -> when (value.lowercase()) {
            "a" -> Color(0xFF038141) to Color.White
            "b" -> Color(0xFF85BB2F) to Color.White
            "c" -> Color(0xFFFECB02) to Color.Black
            "d" -> Color(0xFFEE8100) to Color.White
            "e" -> Color(0xFFE63E11) to Color.White
            else -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = type,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HealthScoreBadgePreview() {
    ShelfLifeTheme {
        HealthScoreBadge(type = "NutriScore", value = "A")
    }
}