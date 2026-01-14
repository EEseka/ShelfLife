package com.eeseka.shelflife.shared.presentation.media

import androidx.compose.runtime.Composable
import com.eeseka.shelflife.shared.domain.media.PickedImage

interface MediaPicker {
    suspend fun pickImage(): PickedImage?
    suspend fun captureImage(): PickedImage?
}

@Composable
expect fun rememberMediaPicker(): MediaPicker