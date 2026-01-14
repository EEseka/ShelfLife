package com.eeseka.shelflife.pantry.presentation.pantry_detail

import com.eeseka.shelflife.shared.presentation.util.UiText

sealed interface PantryDetailEvent {
    data class Error(val message: UiText) : PantryDetailEvent
    data class Success(val message: UiText) : PantryDetailEvent
}