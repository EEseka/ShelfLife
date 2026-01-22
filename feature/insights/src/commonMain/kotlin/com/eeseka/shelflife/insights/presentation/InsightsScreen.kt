package com.eeseka.shelflife.insights.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.insights.presentation.components.EmptyInsightsView
import com.eeseka.shelflife.insights.presentation.components.HealthStatsCard
import com.eeseka.shelflife.insights.presentation.components.InsightFilterRow
import com.eeseka.shelflife.insights.presentation.components.InsightItemCard
import com.eeseka.shelflife.insights.presentation.components.InsightListShimmer
import com.eeseka.shelflife.insights.presentation.components.InsightSummaryCard
import com.eeseka.shelflife.shared.design_system.components.ShelfLifeScaffold
import com.eeseka.shelflife.shared.presentation.util.DeviceConfiguration
import com.eeseka.shelflife.shared.presentation.util.ObserveAsEvents
import com.eeseka.shelflife.shared.presentation.util.ShelfLifeSnackbarVisuals
import com.eeseka.shelflife.shared.presentation.util.SnackbarType
import com.eeseka.shelflife.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import shelflife.feature.insights.generated.resources.Res
import shelflife.feature.insights.generated.resources.cancel
import shelflife.feature.insights.generated.resources.clear_all
import shelflife.feature.insights.generated.resources.clear_history
import shelflife.feature.insights.generated.resources.delete_all_insights_confirm
import shelflife.feature.insights.generated.resources.deleting
import shelflife.feature.insights.generated.resources.history
import shelflife.feature.insights.generated.resources.insights
import shelflife.feature.insights.generated.resources.more_options

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    state: InsightState,
    events: Flow<InsightEvent>,
    onAction: (InsightAction) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = LocalHapticFeedback.current
    val config = currentDeviceConfiguration()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    ObserveAsEvents(events) { event ->
        when (event) {
            is InsightEvent.Success -> {
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        message = event.message.asStringAsync(),
                        type = SnackbarType.Success
                    )
                )
            }

            is InsightEvent.Error -> {
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        message = event.message.asStringAsync(),
                        type = SnackbarType.Error
                    )
                )
            }
        }
    }

    ShelfLifeScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            InsightTopBar(
                showAction = state.items.isNotEmpty(),
                showMenu = showMenu,
                onShowMenuChange = { showMenu = it },
                showDeleteDialog = { showDeleteDialog = true }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        val contentState = when {
            state.isLoading -> InsightScreenState.Loading
            state.totalItems == 0 -> InsightScreenState.Empty
            else -> InsightScreenState.Content
        }

        AnimatedContent(
            targetState = contentState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "InsightContentTransition",
            modifier = Modifier.fillMaxSize()
        ) { targetState ->
            when (targetState) {
                InsightScreenState.Loading -> InsightListShimmer()
                InsightScreenState.Empty -> EmptyInsightsView()

                InsightScreenState.Content -> {
                    InsightContentAdaptive(
                        state = state,
                        config = config,
                        onAction = onAction
                    )
                }
            }
        }

        // --- Dialogs ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!state.isDeleting) showDeleteDialog = false
                },
                title = { Text(stringResource(Res.string.clear_history)) },
                text = {
                    if (state.isDeleting) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(Res.string.deleting))
                        }
                    } else {
                        Text(stringResource(Res.string.delete_all_insights_confirm))
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !state.isDeleting,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onAction(InsightAction.OnClearHistoryClick)
                            showDeleteDialog = false
                        }
                    ) {
                        Text(
                            stringResource(Res.string.clear_all),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = if (!state.isDeleting) {
                    {
                        TextButton(onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                            showDeleteDialog = false
                        }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }
                } else null
            )
        }
    }
}

@Composable
private fun InsightContentAdaptive(
    state: InsightState,
    config: DeviceConfiguration,
    onAction: (InsightAction) -> Unit
) {
    val hasHealthData = state.nutriScoreStats.isNotEmpty() ||
            state.ultraProcessedCount > 0 ||
            state.wastedGoodEcoCount > 0

    if (config.isMobile) {
        // MOBILE LAYOUT (Vertical Column)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InsightFilterRow(
                    selectedTimeFilter = state.selectedTimeFilter,
                    onFilterChange = { onAction(InsightAction.OnTimeFilterChange(it)) }
                )
            }

            item {
                InsightSummaryCard(
                    consumedPercentage = state.consumedPercentage,
                    consumedCount = state.consumedCount,
                    wastedCount = state.wastedCount
                )
            }

            if (hasHealthData) {
                item {
                    HealthStatsCard(
                        nutriScoreDistribution = state.nutriScoreStats,
                        ultraProcessedCount = state.ultraProcessedCount,
                        wastedGoodEcoCount = state.wastedGoodEcoCount
                    )
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(state.items, key = { it.id }) { item ->
                InsightItemCard(item)
            }
        }
    } else {
        // TABLET/LANDSCAPE LAYOUT (Split View)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Pane: Controls & Summary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InsightFilterRow(
                        selectedTimeFilter = state.selectedTimeFilter,
                        onFilterChange = { onAction(InsightAction.OnTimeFilterChange(it)) }
                    )

                    InsightSummaryCard(
                        consumedPercentage = state.consumedPercentage,
                        consumedCount = state.consumedCount,
                        wastedCount = state.wastedCount
                    )

                    if (hasHealthData) {
                        HealthStatsCard(
                            nutriScoreDistribution = state.nutriScoreStats,
                            ultraProcessedCount = state.ultraProcessedCount,
                            wastedGoodEcoCount = state.wastedGoodEcoCount
                        )
                    }
                }

                // Right Pane: History List
                Column(
                    modifier = Modifier.weight(1.5f).fillMaxHeight()
                ) {
                    Text(
                        text = stringResource(Res.string.history),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            InsightItemCard(item)
                        }
                    }
                }
            }
        }
    }
}

private enum class InsightScreenState {
    Loading, Empty, Content
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InsightTopBar(
    showAction: Boolean,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    showDeleteDialog: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.insights),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        actions = {
            if (showAction) {
                Box {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        onShowMenuChange(true)
                    }) {
                        Icon(Icons.Default.MoreVert, stringResource(Res.string.more_options))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { onShowMenuChange(false) }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(Res.string.clear_history),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                onShowMenuChange(false)
                                showDeleteDialog()
                            }
                        )
                    }
                }
            }
        }
    )
}