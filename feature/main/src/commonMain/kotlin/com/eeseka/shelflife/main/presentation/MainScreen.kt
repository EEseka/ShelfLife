package com.eeseka.shelflife.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eeseka.shelflife.main.domain.BottomNavigationItem
import com.eeseka.shelflife.shared.data.util.PlatformUtils
import com.eeseka.shelflife.shared.navigation.Screen
import com.eeseka.shelflife.shared.presentation.util.DeviceConfiguration
import com.eeseka.shelflife.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * No-ripple interaction source for iOS to provide native feel
 */
private class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val config = currentDeviceConfiguration()
    // Nav Rail for Wide Screens OR Mobile Landscape
    val showRail = config.isWideScreen || config == DeviceConfiguration.MOBILE_LANDSCAPE

    val isIos = PlatformUtils.getOSName() == "IOS"

    val navBarItemColors = if (isIos) {
        NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface
        )
    }

    val navRailItemColors = if (isIos) {
        NavigationRailItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        NavigationRailItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                BottomNavigationItem.entries.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any {
                        it.route == item.route::class.qualifiedName
                    } == true

                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title.asString()
                            )
                        },
                        label = {
                            Text(
                                text = item.title.asString(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        },
                        colors = navRailItemColors,
                        // Remove ripple for iOS to achieve native feel
                        interactionSource = if (isIos) remember { NoRippleInteractionSource() } else null
                    )
                }
            }
            NavHost(
                navController = navController,
                startDestination = Screen.Pantry,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                composable<Screen.Pantry> {
                    // REPLACE with PantryScreen() which uses ShelfLifeScaffold internally
                    PlaceholderScreen("Pantry")
                }
                composable<Screen.Insights> {
                    // REPLACE with InsightsScreen() which uses ShelfLifeScaffold internally
                    PlaceholderScreen("Insights")
                }
                composable<Screen.Settings> {
                    // REPLACE with SettingsScreen() which uses ShelfLifeScaffold internally
                    PlaceholderScreen("Settings")
                }
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    BottomNavigationItem.entries.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == item.route::class.qualifiedName
                        } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title.asString()
                                )
                            },
                            label = { Text(text = item.title.asString()) },
                            colors = navBarItemColors,
                            // Remove ripple for iOS to achieve native feel
                            interactionSource = if (isIos) remember { NoRippleInteractionSource() } else null
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Pantry,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<Screen.Pantry> {
                    PlaceholderScreen("Pantry")
                }
                composable<Screen.Insights> {
                    PlaceholderScreen("Insights")
                }
                composable<Screen.Settings> {
                    PlaceholderScreen("Settings")
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}