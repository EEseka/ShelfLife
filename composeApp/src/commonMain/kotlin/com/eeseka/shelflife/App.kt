package com.eeseka.shelflife

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eeseka.shelflife.navigation.SetUpNavGraph
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.settings.AppTheme
import com.eeseka.shelflife.shared.presentation.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    onAuthenticationChecked: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isDarkTheme = when (state.theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
    val startDestination = state.startDestination

    LaunchedEffect(state.isCheckingAuth) {
        if (!state.isCheckingAuth) {
            onAuthenticationChecked()
        }
    }

    ShelfLifeTheme(darkTheme = isDarkTheme) {
        if (!state.isCheckingAuth && startDestination != null) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = true, // It enters once, then stays
                enter = fadeIn()
            ) {
                SetUpNavGraph(startDestination = startDestination)
            }
        }
    }
}

// don't forget to remove the Preview dependencies since we might not have any previews in commonMain