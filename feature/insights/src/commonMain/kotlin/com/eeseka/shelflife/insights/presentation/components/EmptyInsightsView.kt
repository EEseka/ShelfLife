package com.eeseka.shelflife.insights.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.insights.generated.resources.Res
import shelflife.feature.insights.generated.resources.empty_insights_description
import shelflife.feature.insights.generated.resources.empty_insights_title
import shelflife.feature.insights.generated.resources.no_insights_found
import shelflife.feature.insights.generated.resources.try_different_filter

private const val ANIMATION_EMPTY_STATS = "empty_stats.json"

@Composable
fun EmptyInsightsView(isFiltered: Boolean) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/$ANIMATION_EMPTY_STATS").decodeToString()
        )
    }

    val boxModifier = if (isFiltered) {
        Modifier.fillMaxWidth().padding(32.dp)
    } else {
        Modifier.fillMaxSize().padding(32.dp)
    }

    val columnModifier = if (isFiltered) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Handle own scrolling
    }

    Box(modifier = boxModifier) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 600.dp)
                .then(columnModifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = 1
                ),
                contentDescription = null,
                modifier = Modifier.size(240.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isFiltered) stringResource(Res.string.no_insights_found)
                    else stringResource(Res.string.empty_insights_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFiltered) stringResource(Res.string.try_different_filter)
                    else stringResource(Res.string.empty_insights_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyInsightsViewPreview() {
    ShelfLifeTheme {
        EmptyInsightsView(isFiltered = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyInsightsViewPreview2() {
    ShelfLifeTheme {
        EmptyInsightsView(isFiltered = true)
    }
}