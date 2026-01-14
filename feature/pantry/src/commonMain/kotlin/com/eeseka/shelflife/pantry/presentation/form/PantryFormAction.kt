package com.eeseka.shelflife.pantry.presentation.form

import com.eeseka.shelflife.pantry.presentation.util.PantryFormMode
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import kotlinx.datetime.LocalDate

sealed interface PantryFormAction {
    // Lifecycle
    data class Init(val item: PantryItem?, val mode: PantryFormMode) : PantryFormAction

    // Field Updates
    data class UpdateName(val name: String) : PantryFormAction
    data class UpdateBrand(val brand: String) : PantryFormAction
    data class UpdateQuantity(val quantity: String) : PantryFormAction
    data class UpdateQuantityUnit(val unit: String) : PantryFormAction
    data class UpdateStorageLocation(val location: StorageLocation) : PantryFormAction
    data class UpdateExpiryDate(val date: LocalDate) : PantryFormAction
    data class UpdatePurchaseDate(val date: LocalDate) : PantryFormAction
    data class UpdateOpenDate(val date: LocalDate?) : PantryFormAction

    // Image
    data class OnImageSelected(val rawPath: String) : PantryFormAction
    data object OnRemoveImage : PantryFormAction

    // Footer
    data object OnSaveClick : PantryFormAction
}