package com.eeseka.shelflife.main.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.eeseka.shelflife.shared.navigation.Screen
import com.eeseka.shelflife.shared.presentation.util.UiText
import shelflife.feature.main.generated.resources.Res
import shelflife.feature.main.generated.resources.insights
import shelflife.feature.main.generated.resources.pantry
import shelflife.feature.main.generated.resources.settings

enum class BottomNavigationItem(
    val route: Screen,
    val title: UiText,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PANTRY(
        route = Screen.Pantry,
        title = UiText.Resource(Res.string.pantry),
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2
    ),
    INSIGHTS(
        route = Screen.Insights,
        title = UiText.Resource(Res.string.insights),
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
    SETTINGS(
        route = Screen.Settings,
        title = UiText.Resource(Res.string.settings),
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}