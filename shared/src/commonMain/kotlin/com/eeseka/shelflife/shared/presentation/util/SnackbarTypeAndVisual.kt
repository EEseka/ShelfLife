package com.eeseka.shelflife.shared.presentation.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

enum class SnackbarType {
    Success,
    Error,
    Info
}

class ShelfLifeSnackbarVisuals(
    override val message: String,
    val type: SnackbarType,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = false
) : SnackbarVisuals