package com.eeseka.shelflife.auth.presentation

import com.eeseka.shelflife.shared.domain.auth.User

sealed interface AuthAction {
    data class OnGoogleSignInSuccess(val user: User.Authenticated?) : AuthAction
    data class OnGoogleSignInFailure(val error: Throwable) : AuthAction
    data object OnGuestClick : AuthAction
}