package com.eeseka.shelflife.auth.presentation

import com.eeseka.shelflife.shared.presentation.util.UiText

sealed interface AuthEvent {
    data class Success(val message: UiText) : AuthEvent
    data class Error(val message: UiText) : AuthEvent
}