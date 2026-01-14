@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)

package com.eeseka.shelflife.pantry.presentation.pantry_list_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.components.PantryScannerSheet
import com.eeseka.shelflife.pantry.presentation.form.PantryFormRoot
import com.eeseka.shelflife.pantry.presentation.pantry_detail.PantryDetailEvent
import com.eeseka.shelflife.pantry.presentation.pantry_detail.PantryDetailScreen
import com.eeseka.shelflife.pantry.presentation.pantry_list.PantryListEvent
import com.eeseka.shelflife.pantry.presentation.pantry_list.PantryListScreen
import com.eeseka.shelflife.pantry.presentation.util.PantryFormMode
import com.eeseka.shelflife.shared.presentation.util.DialogSheetScopedViewModel
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.select_to_view_detail

@Composable
fun PantryListDetailScreen(
    state: PantryState,
    listEvents: Flow<PantryListEvent>,
    detailEvents: Flow<PantryDetailEvent>,
    onAction: (PantryAction) -> Unit,
    onToggleBottomBar: (Boolean) -> Unit
) {
    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective
    )

    val isListHidden =
        scaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden

    // Sync VM -> Navigator
    LaunchedEffect(state.isDetailOpen) {
        if (state.isDetailOpen) {
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
        } else {
            if (scaffoldNavigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) {
                scaffoldNavigator.navigateBack()
            }
        }
    }

    LaunchedEffect(isListHidden) {
        onToggleBottomBar(!isListHidden) // Hide bottom bar in detail screen and show it in list screen
    }

    // Handle Back Button (Detail -> List)
    BackHandler(enabled = scaffoldNavigator.canNavigateBack()) {
        onAction(PantryAction.OnDetailClose)
    }

    // Handle Back Button (Scanner -> Close Scanner)
    BackHandler(enabled = state.isScannerOpen) {
        onAction(PantryAction.OnScannerDismiss)
    }

    // ROOT BOX to handle Overlays
    Box(modifier = Modifier.fillMaxSize()) {
        ListDetailPaneScaffold(
            directive = scaffoldDirective,
            value = scaffoldNavigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    PantryListScreen(
                        state = state,
                        events = listEvents,
                        onAction = onAction
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    // If the scaffold decides to show this pane (Tablet), show the data.
                    // If the scaffold hides this pane (Mobile), this code won't render anyway.
                    if (state.selectedItem != null) {
                        PantryDetailScreen(
                            item = state.selectedItem,
                            isDeleteLoading = state.isDeletingItem,
                            events = detailEvents,
                            onAction = onAction,
                            showBackButton = isListHidden
                        )
                    } else {
                        EmptyDetailPane()
                    }
                }
            }
        )

        AnimatedVisibility(
            visible = state.isScannerOpen,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            PantryScannerSheet(
                onDismiss = { onAction(PantryAction.OnScannerDismiss) },
                onBarcodeDetected = { onAction(PantryAction.OnBarcodeDetected(it)) },
                isLoading = state.isScannerLoading
            )
        }

        if (state.isCreateItemSheetOpen && state.draftItem != null) {
            DialogSheetScopedViewModel(visible = state.isCreateItemSheetOpen) {
                PantryFormRoot(
                    initialItem = state.draftItem,
                    mode = PantryFormMode.Create,
                    isSaving = state.isCreatingNewItem,
                    onDismiss = { onAction(PantryAction.OnCreateSheetDismiss) },
                    onSave = { newItem ->
                        onAction(PantryAction.OnCreateNewItem(newItem))
                    }
                )
            }
        }

        if (state.isEditItemSheetOpen && state.selectedItem != null) {
            DialogSheetScopedViewModel(visible = state.isEditItemSheetOpen) {
                PantryFormRoot(
                    initialItem = state.selectedItem,
                    mode = PantryFormMode.Edit,
                    isSaving = state.isUpdatingItem,
                    onDismiss = { onAction(PantryAction.OnEditSheetDismiss) },
                    onSave = { newItem ->
                        onAction(PantryAction.OnUpdateItem(newItem))
                    }
                )
            }
        }
    }
}

private const val ANIMATION_EMPTY_BOX_DETAIL = "empty_box_for_detail.json"

@Composable
private fun EmptyDetailPane() {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/$ANIMATION_EMPTY_BOX_DETAIL").decodeToString()
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(0.6f)
        ) {
            Image(
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = 1
                ),
                contentDescription = null,
                modifier = Modifier.size(240.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.select_to_view_detail),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}