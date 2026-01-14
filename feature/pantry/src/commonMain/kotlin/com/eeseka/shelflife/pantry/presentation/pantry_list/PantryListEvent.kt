package com.eeseka.shelflife.pantry.presentation.pantry_list

import com.eeseka.shelflife.shared.presentation.util.UiText

sealed interface PantryListEvent {
    data class Error(val message: UiText) : PantryListEvent
    data class Success(val message: UiText) : PantryListEvent
}