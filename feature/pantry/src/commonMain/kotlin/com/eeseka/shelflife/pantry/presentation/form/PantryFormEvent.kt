package com.eeseka.shelflife.pantry.presentation.form

import com.eeseka.shelflife.shared.domain.pantry.PantryItem

sealed interface PantryFormEvent {
    data class Success(val item: PantryItem) : PantryFormEvent
}