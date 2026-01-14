package com.eeseka.shelflife.pantry.presentation.components

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
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.empty_pantry_description
import shelflife.feature.pantry.generated.resources.empty_pantry_title
import shelflife.feature.pantry.generated.resources.no_items_found
import shelflife.feature.pantry.generated.resources.try_different

private const val ANIMATION_EMPTY_BOX = "empty_box.json"

@Composable
fun EmptyPantryView(isSearchActive: Boolean) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/$ANIMATION_EMPTY_BOX").decodeToString()
        )
    }

    // If we are searching, we are inside a LazyGrid (which already scrolls).
    // If we are NOT searching, we are standalone and need to handle landscape scrolling ourselves.
    val boxModifier = if (isSearchActive) {
        Modifier.fillMaxWidth().padding(32.dp)
    } else {
        Modifier.fillMaxSize().padding(32.dp)
    }

    val columnModifier = if (isSearchActive) {
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
                    text = if (isSearchActive) stringResource(Res.string.no_items_found)
                    else stringResource(Res.string.empty_pantry_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isSearchActive) stringResource(Res.string.try_different)
                    else stringResource(Res.string.empty_pantry_description),
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
private fun EmptyPantryViewPreview() {
    ShelfLifeTheme {
        EmptyPantryView(isSearchActive = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyPantryViewPreview2() {
    ShelfLifeTheme {
        EmptyPantryView(isSearchActive = true)
    }
}