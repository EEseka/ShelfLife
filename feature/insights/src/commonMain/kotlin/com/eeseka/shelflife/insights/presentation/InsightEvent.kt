package com.eeseka.shelflife.insights.presentation

import com.eeseka.shelflife.shared.presentation.util.UiText

sealed interface InsightEvent {
    data class Error(val message: UiText) : InsightEvent
    data class Success(val message: UiText) : InsightEvent
}