package com.eeseka.shelflife.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.auth.generated.resources.Res
import shelflife.feature.auth.generated.resources.or

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Text(
            text = stringResource(Res.string.or),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Preview
@Composable
fun OrDividerPreview() {
    ShelfLifeTheme {
        OrDivider()
    }
}
