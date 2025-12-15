package com.eeseka.shelflife.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.auth.generated.resources.Res
import shelflife.feature.auth.generated.resources.shelf_life
import shelflife.feature.auth.generated.resources.your_smart_pantry_assistant

@Composable
fun AuthBranding(
    composition: LottieComposition?,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever
            ),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.shelf_life),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Text(
            text = stringResource(Res.string.your_smart_pantry_assistant),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun AuthBrandingPreview() {
    ShelfLifeTheme {
        AuthBranding(
            composition = null,
            iconSize = 250.dp,
            modifier = Modifier
        )
    }
}