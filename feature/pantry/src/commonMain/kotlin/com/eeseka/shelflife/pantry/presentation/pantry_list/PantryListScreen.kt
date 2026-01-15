package com.eeseka.shelflife.pantry.presentation.pantry_list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.components.EmptyPantryView
import com.eeseka.shelflife.pantry.presentation.components.PantryItemCard
import com.eeseka.shelflife.pantry.presentation.components.PantryListHeader
import com.eeseka.shelflife.pantry.presentation.components.PantryListShimmer
import com.eeseka.shelflife.pantry.presentation.components.SpeedDialFab
import com.eeseka.shelflife.pantry.presentation.pantry_list_detail.PantryAction
import com.eeseka.shelflife.pantry.presentation.pantry_list_detail.PantryState
import com.eeseka.shelflife.shared.design_system.components.ShelfLifeScaffold
import com.eeseka.shelflife.shared.presentation.util.ObserveAsEvents
import com.eeseka.shelflife.shared.presentation.util.ShelfLifeSnackbarVisuals
import com.eeseka.shelflife.shared.presentation.util.SnackbarType
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.my_pantry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryListScreen(
    state: PantryState,
    events: Flow<PantryListEvent>,
    onAction: (PantryAction) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val focusManager = LocalFocusManager.current
    val hapticFeedback = LocalHapticFeedback.current
    var isFabExpanded by remember { mutableStateOf(false) }

    ObserveAsEvents(events) { event ->
        when (event) {
            is PantryListEvent.Success -> {
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        message = event.message.asStringAsync(),
                        type = SnackbarType.Success,
                        withDismissAction = true
                    )
                )
            }

            is PantryListEvent.Error -> {
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        message = event.message.asStringAsync(),
                        type = SnackbarType.Error,
                        withDismissAction = true
                    )
                )
            }
        }
    }

    ShelfLifeScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                if (isFabExpanded) isFabExpanded = false
            },
        snackbarHostState = snackbarHostState,
        topBar = {
            PantryListTopBar(scrollBehavior = scrollBehavior)
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Had to include this box because SearchBar kept autofocusing, and it's not needed
            // This catches the initial focus so the Keyboard doesn't pop up.
            Box(
                modifier = Modifier.size(1.dp).focusable() // Makes this the first focusable item
            )
            // Content List
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    onAction(PantryAction.OnRefresh)
                },
                modifier = Modifier.fillMaxSize()
            ) {
                val contentState = when {
                    state.isLoading -> PantryScreenState.Loading
                    state.items.isEmpty() && state.searchQuery.isBlank() && state.selectedLocationFilter == null -> PantryScreenState.Empty
                    else -> PantryScreenState.Grid
                }
                AnimatedContent(
                    targetState = contentState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "MainContentTransition",
                    modifier = Modifier.fillMaxSize()
                ) { targetState ->
                    when (targetState) {
                        PantryScreenState.Loading -> {
                            PantryListShimmer()
                        }

                        PantryScreenState.Empty -> {
                            EmptyPantryView(isSearchActive = false)
                        }

                        PantryScreenState.Grid -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 100.dp // Space for FAB
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Header (Search + Chips) - Item 0
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        PantryListHeader(
                                            searchQuery = state.searchQuery,
                                            onQueryChange = {
                                                onAction(
                                                    PantryAction.OnSearchQueryChange(
                                                        it
                                                    )
                                                )
                                            },
                                            selectedLocation = state.selectedLocationFilter,
                                            onLocationChange = {
                                                onAction(
                                                    PantryAction.OnLocationFilterChange(
                                                        it
                                                    )
                                                )
                                            },
                                            onClearSearchAndFocus = {
                                                onAction(PantryAction.OnSearchQueryChange(""))
                                                focusManager.clearFocus()
                                            },
                                            onClearFocus = { focusManager.clearFocus() },
                                            modifier = Modifier.widthIn(max = 600.dp)
                                        )
                                    }
                                }
                                if (state.items.isEmpty()) {
                                    // Search Empty State (Has search term but no results)
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Box(
                                            modifier = Modifier.animateItem().padding(top = 48.dp)
                                        ) {
                                            EmptyPantryView(isSearchActive = true)
                                        }
                                    }
                                } else {
                                    items(state.items, key = { it.id }) { item ->
                                        Box(modifier = Modifier.animateItem()) {
                                            PantryItemCard(
                                                item = item,
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    if (isFabExpanded) isFabExpanded = false
                                                    else onAction(PantryAction.OnItemClick(item))
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Speed Dial FAB
            SpeedDialFab(
                isExpanded = isFabExpanded,
                onToggle = { isFabExpanded = !isFabExpanded },
                onScan = {
                    isFabExpanded = false
                    onAction(PantryAction.OnScannerClick)
                },
                onManual = {
                    isFabExpanded = false
                    onAction(PantryAction.OnManualEntryClick)
                },
                onDismiss = { isFabExpanded = false }
            )
        }
    }
}

private enum class PantryScreenState {
    Loading, Empty, Grid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryListTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.my_pantry),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}