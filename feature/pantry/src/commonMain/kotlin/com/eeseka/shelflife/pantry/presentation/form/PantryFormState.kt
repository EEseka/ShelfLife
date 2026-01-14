package com.eeseka.shelflife.pantry.presentation.form

import androidx.compose.runtime.Immutable
import com.eeseka.shelflife.pantry.presentation.util.PantryFormMode
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import com.eeseka.shelflife.shared.presentation.util.UiText
import kotlinx.datetime.LocalDate

@Immutable
data class PantryFormState(
    // Meta
    val mode: PantryFormMode = PantryFormMode.Create,
    val originalItem: PantryItem? = null,

    // Fields (Editable)
    val name: String = "",
    val brand: String = "",
    val quantity: String = "",
    val quantityUnit: String = "",
    val storageLocation: StorageLocation = StorageLocation.PANTRY,
    val expiryDate: LocalDate? = null,
    val purchaseDate: LocalDate? = null,
    val openDate: LocalDate? = null,
    val imageUrl: String? = null,

    // UI State
    val isCompressingImage: Boolean = false,
    val isFormValid: Boolean = false,
    val hasChanges: Boolean = false,
    val isExpiryDateInPast: Boolean = false,
    val formValidationError: UiText? = null
)