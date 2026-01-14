package com.eeseka.shelflife.pantry.presentation.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eeseka.shelflife.pantry.presentation.util.PantryFormMode
import com.eeseka.shelflife.shared.design_system.components.ShelfLifeAdaptiveSheetLayout
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.presentation.util.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PantryFormRoot(
    initialItem: PantryItem,
    mode: PantryFormMode,
    isSaving: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (PantryItem) -> Unit,
    viewModel: PantryFormViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Initialize the form with initial data
    LaunchedEffect(initialItem.id, mode) {
        viewModel.onAction(
            PantryFormAction.Init(
                item = initialItem,
                mode = mode
            )
        )
    }

    // Observe events from the ViewModel
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is PantryFormEvent.Success -> onSave(event.item)
        }
    }

    ShelfLifeAdaptiveSheetLayout(
        onDismiss = onDismiss
    ) {
        PantryItemFormSheetContent(
            state = state,
            isSaving = isSaving,
            onAction = viewModel::onAction,
            onDismiss = onDismiss
        )
    }
}