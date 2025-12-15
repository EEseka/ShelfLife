package com.eeseka.shelflife.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eeseka.shelflife.auth.presentation.AuthAction
import com.eeseka.shelflife.auth.presentation.AuthScreen
import com.eeseka.shelflife.auth.presentation.AuthViewModel
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
                events = viewModel.events,
                onGoogleSignInSuccess = {
                    viewModel.onAction(AuthAction.OnGoogleSignInSuccess(it))
                },
                onGoogleSignInFailure = {
                    viewModel.onAction(AuthAction.OnGoogleSignInFailure(it))
                }
            )
        }

        composable<Screen.HomeGraph> {
            PlaceholderScreen("Home Graph\n(Pantry/Scanner goes here)")
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Does the theme work?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = "It does!",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}