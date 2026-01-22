package com.eeseka.shelflife.pantry.presentation.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.pantry.presentation.util.PantryFormMode
import com.eeseka.shelflife.pantry.presentation.util.clean
import com.eeseka.shelflife.shared.domain.media.ImageCompressionService
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import com.eeseka.shelflife.shared.presentation.util.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.error_expiry_required
import shelflife.feature.pantry.generated.resources.error_name_required
import shelflife.feature.pantry.generated.resources.error_quantity_required
import shelflife.feature.pantry.generated.resources.error_quantity_zero
import shelflife.feature.pantry.generated.resources.error_unit_required
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class PantryFormViewModel(
    private val imageCompressor: ImageCompressionService
) : ViewModel() {

    private val _state = MutableStateFlow(PantryFormState())
    val state = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PantryFormState()
    )

    private val _eventChannel = Channel<PantryFormEvent>()
    val events = _eventChannel.receiveAsFlow()

    private var compressedImageUrl: String? = null

    fun onAction(action: PantryFormAction) {
        when (action) {
            is PantryFormAction.Init -> init(action.item, action.mode, action.defaultUnit)
            is PantryFormAction.UpdateName -> onNameChange(action.name)
            is PantryFormAction.UpdateBrand -> onBrandChange(action.brand)
            is PantryFormAction.UpdateQuantity -> onQuantityChange(action.quantity)
            is PantryFormAction.UpdateQuantityUnit -> onQuantityUnitChange(action.unit)
            is PantryFormAction.UpdateStorageLocation -> onStorageLocationChange(action.location)
            is PantryFormAction.UpdateExpiryDate -> onExpiryDateChange(action.date)
            is PantryFormAction.UpdatePurchaseDate -> onPurchaseDateChange(action.date)
            is PantryFormAction.UpdateOpenDate -> onOpenDateChange(action.date)
            is PantryFormAction.OnImageSelected -> onImageSelected(action.rawPath)
            is PantryFormAction.OnRemoveImage -> onRemoveImage()
            is PantryFormAction.OnSaveClick -> onSaveClick()
        }
    }

    private fun init(item: PantryItem?, mode: PantryFormMode, defaultUnit: String) {
        // Only initialize if we haven't been initialized yet (to prevent overriding user edits on rotation)
        if (_state.value.originalItem == null && item != null) {
            _state.update { currentState ->
                val updatedState = currentState.copy(
                    mode = mode,
                    originalItem = item,
                    name = item.name,
                    brand = item.brand ?: "",
                    quantity = item.quantity.clean(),
                    quantityUnit = item.quantityUnit.ifBlank { defaultUnit },
                    storageLocation = item.storageLocation,
                    expiryDate = item.expiryDate,
                    purchaseDate = item.purchaseDate,
                    openDate = item.openDate,
                    imageUrl = item.imageUrl
                )
                updatedState.copy(
                    isFormValid = checkFormValidity(updatedState),
                    formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                        updatedState
                    ) else null,
                    hasChanges = false,
                    isExpiryDateInPast = item.expiryDate < currentDate()
                )
            }
        }
    }

    private fun onNameChange(name: String) {
        _state.update { currentState ->
            val updatedState = currentState.copy(name = name)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState)
            )
        }
    }

    private fun onBrandChange(brand: String) {
        _state.update { currentState ->
            val updatedState = currentState.copy(brand = brand)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState)
            )
        }
    }

    private fun onQuantityChange(quantity: String) {
        // Only allow valid numeric input (digits and one dot)
        if (quantity.count { it == '.' } <= 1 && quantity.all { it.isDigit() || it == '.' }) {
            _state.update { currentState ->
                val updatedState = currentState.copy(quantity = quantity)
                updatedState.copy(
                    isFormValid = checkFormValidity(updatedState),
                    formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                        updatedState
                    ) else null,
                    hasChanges = checkHasChanges(updatedState)
                )
            }
        }
    }

    private fun onQuantityUnitChange(unit: String) {
        _state.update { currentState ->
            val updatedState = currentState.copy(quantityUnit = unit)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState)
            )
        }
    }

    private fun onStorageLocationChange(location: StorageLocation) {
        _state.update { currentState ->
            val updatedState = currentState.copy(storageLocation = location)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState)
            )
        }
    }

    private fun onExpiryDateChange(date: LocalDate) {
        _state.update { currentState ->
            val updatedState = currentState.copy(expiryDate = date)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState),
                isExpiryDateInPast = date < currentDate()
            )
        }
    }

    private fun onPurchaseDateChange(date: LocalDate) {
        _state.update { currentState ->
            val updatedState = currentState.copy(purchaseDate = date)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState)
            )
        }
    }

    private fun onOpenDateChange(date: LocalDate?) {
        _state.update { currentState ->
            val updatedState = currentState.copy(openDate = date)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState)
            )
        }
    }

    private fun onImageSelected(rawPath: String) {
        _state.update {
            it.copy(
                imageUrl = rawPath,
                isCompressingImage = true,
                hasChanges = true
            )
        }

        viewModelScope.launch {
            val compressed = imageCompressor.compress(rawPath)
            compressedImageUrl = compressed

            _state.update { it.copy(isCompressingImage = false) }
        }
    }

    private fun onRemoveImage() {
        compressedImageUrl = null
        _state.update { currentState ->
            val updatedState = currentState.copy(imageUrl = null)
            updatedState.copy(
                isFormValid = checkFormValidity(updatedState),
                formValidationError = if (!checkFormValidity(updatedState)) getFormValidationError(
                    updatedState
                ) else null,
                hasChanges = checkHasChanges(updatedState)
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun onSaveClick() {
        val currentState = _state.value
        // Safety check, though button should be disabled if invalid
        if (!currentState.isFormValid) return

        val original = currentState.originalItem ?: return // Should never be null if initialized

        // Use compressed image if available, otherwise use raw image from state
        val finalImageUrl = compressedImageUrl ?: currentState.imageUrl

        val resultItem = original.copy(
            name = currentState.name,
            brand = currentState.brand.ifBlank { null },
            quantity = currentState.quantity.toDoubleOrNull() ?: 1.0,
            quantityUnit = currentState.quantityUnit,
            storageLocation = currentState.storageLocation,
            expiryDate = currentState.expiryDate ?: currentDate(),
            purchaseDate = currentState.purchaseDate ?: currentDate(),
            openDate = currentState.openDate,
            imageUrl = finalImageUrl,
            thumbnailUrl = if (finalImageUrl != original.imageUrl) null else original.thumbnailUrl
        )

        viewModelScope.launch {
            _eventChannel.send(PantryFormEvent.Success(resultItem))
        }
    }

    private fun checkFormValidity(state: PantryFormState): Boolean {
        return with(state) {
            val isNameValid = name.isNotBlank()
            val isQuantityValid = quantity.toDoubleOrNull()?.let { it > 0.0 } ?: false
            val isQuantityUnitValid = quantityUnit.isNotBlank()
            val isExpiryValid = expiryDate != null

            isNameValid && isQuantityValid && isQuantityUnitValid && isExpiryValid
        }
    }

    private fun getFormValidationError(state: PantryFormState): UiText? {
        return with(state) {
            when {
                name.isBlank() -> UiText.Resource(Res.string.error_name_required)
                quantity.isBlank() -> UiText.Resource(Res.string.error_quantity_required)
                quantity.toDoubleOrNull()
                    ?.let { it <= 0.0 } == true -> UiText.Resource(Res.string.error_quantity_zero)

                quantityUnit.isBlank() -> UiText.Resource(Res.string.error_unit_required)
                expiryDate == null -> UiText.Resource(Res.string.error_expiry_required)
                else -> null
            }
        }
    }

    private fun checkHasChanges(state: PantryFormState): Boolean {
        val original = state.originalItem ?: return false

        // In Create mode: always return true since we're creating a new item
        // The form is initialized with defaults and any valid form state represents a new item to create
        if (state.mode == PantryFormMode.Create) return true

        // In Edit mode: check if any field differs from the original item
        val currentQuantity = state.quantity.toDoubleOrNull() ?: 1.0
        val currentBrand = state.brand.ifBlank { null }

        return state.name != original.name ||
                currentBrand != original.brand ||
                state.imageUrl != original.imageUrl ||
                state.expiryDate != original.expiryDate ||
                state.storageLocation != original.storageLocation ||
                currentQuantity != original.quantity ||
                state.quantityUnit != original.quantityUnit ||
                state.purchaseDate != original.purchaseDate ||
                state.openDate != original.openDate
    }

    @OptIn(ExperimentalTime::class)
    private fun currentDate() =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}