package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

@Composable
fun HeroImageSection(
    imageUrl: String?,
    thumbnailUrl: String?,
    contentDescription: String,
    imageHeight: Dp
) {
    AsyncImage(
        model = imageUrl ?: thumbnailUrl,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        placeholder = rememberVectorPainter(Icons.Outlined.Image),
        error = rememberVectorPainter(Icons.Outlined.ImageNotSupported),
        modifier = Modifier
            .fillMaxWidth()
            .height(imageHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .alpha(if (imageUrl == null && thumbnailUrl == null) 0.5f else 1f)
    )
}