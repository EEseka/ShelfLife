package com.eeseka.shelflife.pantry.presentation.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.components.FormDatePickerField
import com.eeseka.shelflife.pantry.presentation.components.ImagePickerSection
import com.eeseka.shelflife.pantry.presentation.components.ImageSourceDialog
import com.eeseka.shelflife.pantry.presentation.components.StorageLocationSelector
import com.eeseka.shelflife.pantry.presentation.components.previewPantryItem
import com.eeseka.shelflife.pantry.presentation.util.PantryFormMode
import com.eeseka.shelflife.pantry.presentation.util.clean
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.presentation.media.rememberMediaPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.add_item
import shelflife.feature.pantry.generated.resources.add_to_pantry
import shelflife.feature.pantry.generated.resources.adding
import shelflife.feature.pantry.generated.resources.brand_label
import shelflife.feature.pantry.generated.resources.brand_placeholder
import shelflife.feature.pantry.generated.resources.cancel
import shelflife.feature.pantry.generated.resources.edit_item
import shelflife.feature.pantry.generated.resources.expiry_date
import shelflife.feature.pantry.generated.resources.expiry_date_past_error
import shelflife.feature.pantry.generated.resources.grams_unit
import shelflife.feature.pantry.generated.resources.item_name
import shelflife.feature.pantry.generated.resources.item_name_placeholder
import shelflife.feature.pantry.generated.resources.kilograms_unit
import shelflife.feature.pantry.generated.resources.liters_unit
import shelflife.feature.pantry.generated.resources.milliliters_unit
import shelflife.feature.pantry.generated.resources.opened_date
import shelflife.feature.pantry.generated.resources.pieces_unit
import shelflife.feature.pantry.generated.resources.purchase_date
import shelflife.feature.pantry.generated.resources.quantity
import shelflife.feature.pantry.generated.resources.save_changes
import shelflife.feature.pantry.generated.resources.saving
import shelflife.feature.pantry.generated.resources.unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryItemFormSheetContent(
    state: PantryFormState,
    isSaving: Boolean = false,
    onAction: (PantryFormAction) -> Unit,
    onDismiss: () -> Unit
) {
    // --- Helpers ---
    val mediaPicker = rememberMediaPicker()
    val scope = rememberCoroutineScope()
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val quickUnits = listOf(
        stringResource(Res.string.pieces_unit),
        stringResource(Res.string.kilograms_unit),
        stringResource(Res.string.grams_unit),
        stringResource(Res.string.liters_unit),
        stringResource(Res.string.milliliters_unit)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
            .padding(24.dp)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.mode == PantryFormMode.Create) stringResource(Res.string.add_item)
                else stringResource(Res.string.edit_item),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                enabled = !isSaving,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    onDismiss()
                }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.cancel)
                )
            }
        }

        // --- Scrollable Form ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Picker (Always Visible)
            ImagePickerSection(
                imageUri = state.imageUrl,
                onImagePick = { showImageSourceDialog = true },
                onImageRemove = { onAction(PantryFormAction.OnRemoveImage) }
            )

            // Core Details (Always Visible)
            OutlinedTextField(
                value = state.name,
                onValueChange = { onAction(PantryFormAction.UpdateName(it)) },
                label = { Text(stringResource(Res.string.item_name)) },
                placeholder = { Text(stringResource(Res.string.item_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = state.brand,
                onValueChange = { onAction(PantryFormAction.UpdateBrand(it)) },
                label = { Text(stringResource(Res.string.brand_label)) },
                placeholder = { Text(stringResource(Res.string.brand_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = if (state.mode == PantryFormMode.Create) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (state.mode == PantryFormMode.Create) focusManager.clearFocus() }
                ),
                shape = MaterialTheme.shapes.medium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = {
                            onAction(PantryFormAction.UpdateQuantity(it))
                        },
                        label = { Text(stringResource(Res.string.quantity)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = state.quantityUnit,
                        onValueChange = { onAction(PantryFormAction.UpdateQuantityUnit(it)) },
                        label = { Text(stringResource(Res.string.unit)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = MaterialTheme.shapes.medium
                    )
                }
                // Quick Chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickUnits.forEach { unit ->
                        val isSelected = state.quantityUnit.equals(unit, ignoreCase = true)
                        InputChip(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    onAction(PantryFormAction.UpdateQuantityUnit(unit))
                                }
                            },
                            label = { Text(unit) },
                            border = null,
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Dates Section
            // Expiry is ALWAYS visible (Critical)
            FormDatePickerField(
                label = stringResource(Res.string.expiry_date),
                date = state.expiryDate,
                onDateSelected = { onAction(PantryFormAction.UpdateExpiryDate(it)) },
                icon = Icons.Default.CalendarToday,
                isError = state.isExpiryDateInPast,
                errorMessage = stringResource(Res.string.expiry_date_past_error)
            )

            // EDIT MODE ONLY: Purchase Date & Open Date
            if (state.mode == PantryFormMode.Edit) {
                FormDatePickerField(
                    label = stringResource(Res.string.purchase_date),
                    date = state.purchaseDate,
                    onDateSelected = { onAction(PantryFormAction.UpdatePurchaseDate(it)) },
                    icon = Icons.Outlined.CalendarToday
                )

                FormDatePickerField(
                    label = stringResource(Res.string.opened_date),
                    date = state.openDate,
                    onDateSelected = { onAction(PantryFormAction.UpdateOpenDate(it)) },
                    icon = Icons.Outlined.LockOpen,
                    isClearable = true,
                    onClear = { onAction(PantryFormAction.UpdateOpenDate(null)) }
                )
            }

            // Only Divider if we showed extra dates, otherwise it's clean
            if (state.mode == PantryFormMode.Edit) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            // Storage Location (Always Visible)
            StorageLocationSelector(
                currentLocation = state.storageLocation,
                onLocationSelected = { onAction(PantryFormAction.UpdateStorageLocation(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Footer: Save Button ---
        Spacer(modifier = Modifier.height(16.dp))

        if (state.formValidationError != null && state.hasChanges) {
            Text(
                text = state.formValidationError.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                focusManager.clearFocus()
                onAction(PantryFormAction.OnSaveClick)
            },
            enabled = state.isFormValid && state.hasChanges && !state.isCompressingImage && !isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = when {
                        isSaving && state.mode == PantryFormMode.Create -> stringResource(Res.string.adding)
                        isSaving && state.mode == PantryFormMode.Edit -> stringResource(Res.string.saving)
                        state.mode == PantryFormMode.Create -> stringResource(Res.string.add_to_pantry)
                        else -> stringResource(Res.string.save_changes)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }

    // --- Dialogs ---
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraClick = {
                scope.launch {
                    showImageSourceDialog = false
                    val result = mediaPicker.captureImage()
                    if (result != null) {
                        onAction(PantryFormAction.OnImageSelected(result.uri))
                    }
                }
            },
            onGalleryClick = {
                scope.launch {
                    showImageSourceDialog = false
                    val result = mediaPicker.pickImage()
                    if (result != null) {
                        onAction(PantryFormAction.OnImageSelected(result.uri))
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreatePantryItemSheetContentPreview() {
    ShelfLifeTheme {
        PantryItemFormSheetContent(
            state = PantryFormState(
                mode = PantryFormMode.Create,
                originalItem = previewPantryItem,
                name = previewPantryItem.name,
                brand = previewPantryItem.brand ?: "",
                quantity = previewPantryItem.quantity.clean(),
                quantityUnit = previewPantryItem.quantityUnit,
                storageLocation = previewPantryItem.storageLocation,
                expiryDate = previewPantryItem.expiryDate,
                imageUrl = previewPantryItem.imageUrl,
                isFormValid = true,
                hasChanges = true
            ),
            isSaving = false,
            onAction = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun CreatePantryItemSheetContentPreviewDark() {
    ShelfLifeTheme(true) {
        PantryItemFormSheetContent(
            state = PantryFormState(
                mode = PantryFormMode.Create,
                originalItem = previewPantryItem,
                name = previewPantryItem.name,
                brand = previewPantryItem.brand ?: "",
                quantity = previewPantryItem.quantity.clean(),
                quantityUnit = previewPantryItem.quantityUnit,
                storageLocation = previewPantryItem.storageLocation,
                expiryDate = previewPantryItem.expiryDate,
                imageUrl = previewPantryItem.imageUrl,
                isFormValid = true,
                hasChanges = true
            ),
            isSaving = false,
            onAction = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdatePantryItemSheetContentPreview() {
    ShelfLifeTheme {
        PantryItemFormSheetContent(
            state = PantryFormState(
                mode = PantryFormMode.Edit,
                originalItem = previewPantryItem,
                name = previewPantryItem.name,
                brand = previewPantryItem.brand ?: "",
                quantity = previewPantryItem.quantity.clean(),
                quantityUnit = previewPantryItem.quantityUnit,
                storageLocation = previewPantryItem.storageLocation,
                expiryDate = previewPantryItem.expiryDate,
                purchaseDate = previewPantryItem.purchaseDate,
                openDate = previewPantryItem.openDate,
                imageUrl = previewPantryItem.imageUrl,
                isFormValid = true,
                hasChanges = false
            ),
            isSaving = false,
            onAction = { },
            onDismiss = { }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun UpdatePantryItemSheetContentPreviewDark() {
    ShelfLifeTheme(true) {
        PantryItemFormSheetContent(
            state = PantryFormState(
                mode = PantryFormMode.Edit,
                originalItem = previewPantryItem,
                name = previewPantryItem.name,
                brand = previewPantryItem.brand ?: "",
                quantity = previewPantryItem.quantity.clean(),
                quantityUnit = previewPantryItem.quantityUnit,
                storageLocation = previewPantryItem.storageLocation,
                expiryDate = previewPantryItem.expiryDate,
                purchaseDate = previewPantryItem.purchaseDate,
                openDate = previewPantryItem.openDate,
                imageUrl = previewPantryItem.imageUrl,
                isFormValid = true,
                hasChanges = false
            ),
            isSaving = false,
            onAction = { },
            onDismiss = { }
        )
    }
}