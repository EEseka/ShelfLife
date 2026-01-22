package com.eeseka.shelflife.pantry.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun CameraPermissionGuard(
    onPermissionDenied: @Composable (openSettings: () -> Unit) -> Unit,
    onPermissionGranted: @Composable () -> Unit
)