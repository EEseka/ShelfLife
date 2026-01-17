package com.eeseka.shelflife.insights.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.insights.presentation.util.TimeFilter

@Composable
fun InsightFilterRow(
    selectedTimeFilter: TimeFilter,
    onFilterChange: (TimeFilter) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selectedTimeFilter,
                onClick = {
                    if (selectedTimeFilter != filter) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        onFilterChange(filter)
                    }
                },
                label = { Text(filter.label.asString()) }
            )
        }
    }
}