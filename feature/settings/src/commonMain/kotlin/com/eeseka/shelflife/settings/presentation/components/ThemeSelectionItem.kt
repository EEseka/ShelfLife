package com.eeseka.shelflife.settings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.settings.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.settings.generated.resources.Res
import shelflife.feature.settings.generated.resources.app_theme
import shelflife.feature.settings.generated.resources.dark
import shelflife.feature.settings.generated.resources.light
import shelflife.feature.settings.generated.resources.system

@Composable
fun ThemeSelectionItem(currentTheme: AppTheme, onThemeSelected: (AppTheme) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.app_theme),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
                .padding(4.dp)
        ) {
            ThemeOption(
                label = stringResource(Res.string.system),
                isSelected = currentTheme == AppTheme.SYSTEM,
                onClick = { onThemeSelected(AppTheme.SYSTEM) }
            )
            ThemeOption(
                label = stringResource(Res.string.light),
                isSelected = currentTheme == AppTheme.LIGHT,
                onClick = { onThemeSelected(AppTheme.LIGHT) }
            )
            ThemeOption(
                label = stringResource(Res.string.dark),
                isSelected = currentTheme == AppTheme.DARK,
                onClick = { onThemeSelected(AppTheme.DARK) }
            )
        }
    }
}

@Composable
private fun ThemeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent
    val textColor =
        if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
    val shadowElevation = if (isSelected) 1.dp else 0.dp

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .shadow(shadowElevation)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeSelectionItemPreview() {
    ShelfLifeTheme {
        ThemeSelectionItem(currentTheme = AppTheme.SYSTEM, onThemeSelected = {})
    }
}