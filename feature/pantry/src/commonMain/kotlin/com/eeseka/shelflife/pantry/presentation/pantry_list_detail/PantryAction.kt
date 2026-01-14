package com.eeseka.shelflife.pantry.presentation.pantry_list_detail

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation

sealed interface PantryAction {
    // --- Data Sync ---
    data object OnRefresh : PantryAction

    // --- List Interactions ---
    data class OnSearchQueryChange(val query: String) : PantryAction
    data class OnLocationFilterChange(val location: StorageLocation?) : PantryAction
    data class OnItemClick(val item: PantryItem) : PantryAction

    // --- FAB, Scanner and Sheet Flow ---
    data object OnScannerClick : PantryAction
    data object OnScannerDismiss : PantryAction
    data class OnBarcodeDetected(val barcode: String) : PantryAction
    data object OnManualEntryClick : PantryAction
    data object OnCreateSheetDismiss : PantryAction
    data object OnEditSheetOpen : PantryAction
    data object OnEditSheetDismiss : PantryAction

    // --- Detail Screen Interactions ---
    data object OnDetailClose : PantryAction
    data class OnCreateNewItem(val item: PantryItem) : PantryAction
    data class OnUpdateItem(val item: PantryItem) : PantryAction
    data class OnDeleteItem(val id: String) : PantryAction
}