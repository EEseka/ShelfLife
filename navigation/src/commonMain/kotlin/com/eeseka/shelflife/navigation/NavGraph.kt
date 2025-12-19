package com.eeseka.shelflife.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eeseka.shelflife.auth.presentation.AuthScreen
import com.eeseka.shelflife.auth.presentation.AuthViewModel
import com.eeseka.shelflife.main.presentation.MainScreen
import com.eeseka.shelflife.onboarding.presentation.OnboardingScreen
import com.eeseka.shelflife.onboarding.presentation.OnboardingViewModel
import com.eeseka.shelflife.shared.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SetUpNavGraph(
    startDestination: Screen
) {
    val navController = rememberNavController()

    LaunchedEffect(startDestination) {
        startDestination.let { destination ->
            if (navController.currentBackStackEntry?.destination?.route != destination::class.qualifiedName) {
                navController.navigate(destination) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Onboarding> {
            val viewModel = koinViewModel<OnboardingViewModel>()
            OnboardingScreen(
                onAction = viewModel::onAction
            )
        }

        composable<Screen.Auth> {
            val viewModel = koinViewModel<AuthViewModel>()
            AuthScreen(
                onAction = viewModel::onAction,
                events = viewModel.events
            )
        }

        composable<Screen.HomeGraph> {
            // Contains its own NavHost
            MainScreen()
        }
    }
}