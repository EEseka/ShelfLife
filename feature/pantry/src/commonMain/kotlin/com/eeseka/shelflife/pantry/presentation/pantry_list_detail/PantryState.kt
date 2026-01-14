package com.eeseka.shelflife.pantry.presentation.pantry_list_detail

import androidx.compose.runtime.Immutable
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation

@Immutable
data class PantryState(
    // --- List Screen Data ---
    val items: List<PantryItem> = emptyList(),
    val searchQuery: String = "",
    val selectedLocationFilter: StorageLocation? = null,

    // --- Loading States ---
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isScannerLoading: Boolean = false,
    val isCreatingNewItem: Boolean = false,
    val isUpdatingItem: Boolean = false,
    val isDeletingItem: Boolean = false,

    // --- Scanner / Creation & Update Sheet ---
    val isScannerOpen: Boolean = false,
    val isCreateItemSheetOpen: Boolean = false,
    val isEditItemSheetOpen: Boolean = false,

    // -- Draft Item (For new Pantry Items) ---
    val draftItem: PantryItem? = null,

    // --- Detail / Selection Data ---
    val selectedItem: PantryItem? = null,
    val isDetailOpen: Boolean = false,

    // -- For the Dialog Adaptive Sheet ---
    val createSheetScopeId: String? = null,
    val editSheetScopeId: String? = null
)