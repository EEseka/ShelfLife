package com.eeseka.shelflife.insights.presentation

import androidx.compose.runtime.Immutable
import com.eeseka.shelflife.insights.presentation.util.TimeFilter
import com.eeseka.shelflife.shared.domain.insight.InsightItem

@Immutable
data class InsightState(
    // --- Screen Data ---
    val items: List<InsightItem> = emptyList(),
    val selectedTimeFilter: TimeFilter = TimeFilter.ALL_TIME,

    // --- Derived Stats ---
    val consumedCount: Int = 0,
    val wastedCount: Int = 0,
    val consumedPercentage: Float = 0f, // 0.0 to 1.0

    // --- Health Analytics ---
    val nutriScoreStats: Map<String, Int> = emptyMap(),
    val ultraProcessedCount: Int = 0,
    val wastedGoodEcoCount: Int = 0,

    // --- Loading States ---
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false
)