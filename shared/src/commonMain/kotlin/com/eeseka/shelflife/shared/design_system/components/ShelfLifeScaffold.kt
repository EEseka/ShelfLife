package com.eeseka.shelflife.shared.design_system.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.presentation.util.ShelfLifeSnackbarVisuals
import com.eeseka.shelflife.shared.presentation.util.SnackbarType

@Composable
fun ShelfLifeScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        contentWindowInsets = WindowInsets.statusBars
            .union(WindowInsets.displayCutout)
            .union(WindowInsets.ime),
        snackbarHost = {
            snackbarHostState?.let {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) { data ->
                    val visuals = data.visuals as? ShelfLifeSnackbarVisuals
                    val type = visuals?.type ?: SnackbarType.Info

                    val containerColor = when (type) {
                        SnackbarType.Success -> MaterialTheme.colorScheme.primaryContainer
                        SnackbarType.Error -> MaterialTheme.colorScheme.errorContainer
                        SnackbarType.Info -> MaterialTheme.colorScheme.inverseSurface
                    }

                    val contentColor = when (type) {
                        SnackbarType.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                        SnackbarType.Error -> MaterialTheme.colorScheme.onErrorContainer
                        SnackbarType.Info -> MaterialTheme.colorScheme.inverseOnSurface
                    }

                    Snackbar(
                        snackbarData = data,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        containerColor = containerColor,
                        contentColor = contentColor,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            content()
        }
    }
}