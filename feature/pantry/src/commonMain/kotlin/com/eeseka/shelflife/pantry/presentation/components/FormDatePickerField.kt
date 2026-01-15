package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.cancel
import shelflife.feature.pantry.generated.resources.save
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun FormDatePickerField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    icon: ImageVector,
    isError: Boolean = false,
    errorMessage: String? = null,
    isClearable: Boolean = false,
    onClear: () -> Unit = {}
) {
    var showPicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val displayText = date?.toString() ?: ""

    val hapticFeedback = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        showPicker = true
                    }
                ),
            leadingIcon = { Icon(icon, null) },
            trailingIcon = {
                if (isClearable && date != null) {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        onClear()
                    }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            colors = TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.Transparent,
                disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledIndicatorColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
            ),
            shape = MaterialTheme.shapes.medium
        )
    }

    if (showPicker) {
        val initialMillis =
            date?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()
                ?: Clock.System.now().toEpochMilliseconds()

        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    state.selectedDateMillis?.let { millis ->
                        val newDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        onDateSelected(newDate)
                    }
                    showPicker = false
                }) { Text(stringResource(Res.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    showPicker = false
                }) { Text(stringResource(Res.string.cancel)) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormDatePickerFieldPreview() {
    ShelfLifeTheme {
        FormDatePickerField(
            label = "Expiry Date",
            date = LocalDate(2023, 12, 1),
            onDateSelected = {},
            icon = Icons.Default.Close
        )
    }
}