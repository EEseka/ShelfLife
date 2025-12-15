package com.eeseka.shelflife.auth.presentation

import com.eeseka.shelflife.shared.presentation.util.UiText

sealed interface AuthEvent {
    data class Success(val successMessage: UiText) : AuthEvent
    data class Error(val error: UiText) : AuthEvent
}