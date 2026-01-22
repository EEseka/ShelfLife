package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.all
import shelflife.feature.pantry.generated.resources.close
import shelflife.feature.pantry.generated.resources.freezer
import shelflife.feature.pantry.generated.resources.fridge
import shelflife.feature.pantry.generated.resources.pantry
import shelflife.feature.pantry.generated.resources.search_items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryListHeader(
    searchQuery: String,
    placeholderRes: StringResource,
    onQueryChange: (String) -> Unit,
    selectedLocation: StorageLocation?,
    onLocationChange: (StorageLocation?) -> Unit,
    onClearSearchAndFocus: () -> Unit,
    onClearFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        onClearFocus()
                    },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(placeholderRes)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                                onClearSearchAndFocus()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(Res.string.close)
                                )
                            }
                        }
                    } else null,
                )
            },
            expanded = false,
            onExpandedChange = {},
            windowInsets = WindowInsets(0.dp),
            modifier = Modifier.fillMaxWidth(),
            content = {}
        )

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedLocation == null,
                onClick = {
                    if (selectedLocation != null) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        onLocationChange(null)
                    }
                },
                label = { Text(stringResource(Res.string.all)) },
            )
            StorageLocation.entries.forEach { location ->
                FilterChip(
                    selected = selectedLocation == location,
                    onClick = {
                        if (selectedLocation != location) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            onLocationChange(location)
                        }
                    },
                    label = {
                        Text(
                            when (location) {
                                StorageLocation.PANTRY -> stringResource(Res.string.pantry)
                                StorageLocation.FRIDGE -> stringResource(Res.string.fridge)
                                StorageLocation.FREEZER -> stringResource(Res.string.freezer)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PantryListHeaderPreview() {
    ShelfLifeTheme {
        PantryListHeader(
            searchQuery = "",
            placeholderRes = Res.string.search_items,
            onQueryChange = { },
            selectedLocation = null,
            onLocationChange = { },
            onClearSearchAndFocus = { },
            onClearFocus = { }
        )
    }
}