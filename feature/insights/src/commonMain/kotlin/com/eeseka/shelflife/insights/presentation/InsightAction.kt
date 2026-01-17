package com.eeseka.shelflife.insights.presentation

import com.eeseka.shelflife.insights.presentation.util.TimeFilter

sealed interface InsightAction {
    data class OnTimeFilterChange(val filter: TimeFilter) : InsightAction
    data object OnClearHistoryClick : InsightAction
}