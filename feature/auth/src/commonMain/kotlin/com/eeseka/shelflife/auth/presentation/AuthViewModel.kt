package com.eeseka.shelflife.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.onFailure
import com.eeseka.shelflife.shared.domain.util.onSuccess
import com.eeseka.shelflife.shared.presentation.util.UiText
import com.eeseka.shelflife.shared.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import shelflife.feature.auth.generated.resources.Res
import shelflife.feature.auth.generated.resources.account_disabled
import shelflife.feature.auth.generated.resources.auth_success
import shelflife.feature.auth.generated.resources.internet_connection_unavailable
import shelflife.feature.auth.generated.resources.sign_in_canceled
import shelflife.feature.auth.generated.resources.unknown_error_occurred

class AuthViewModel(
    private val shelfLifeLogger: ShelfLifeLogger,
    private val authService: AuthService
) : ViewModel() {
    private val _eventChannel = Channel<AuthEvent>()
    val events = _eventChannel.receiveAsFlow()

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnGoogleSignInSuccess -> handleGoogleSignInSuccess(action.user)
            is AuthAction.OnGoogleSignInFailure -> sendError(action.error)
            AuthAction.OnGuestClick -> signInAsGuest()
        }
    }

    private fun signInAsGuest() {
        viewModelScope.launch {
            authService.signInAnonymously()
                .onSuccess {
                    // Firebase auth state will update automatically
                    // MainViewModel will cache the user and navigate to Home
                    _eventChannel.send(AuthEvent.Success(UiText.Resource(Res.string.auth_success)))
                }.onFailure { error ->
                    _eventChannel.send(AuthEvent.Error(error.toUiText()))
                }
        }
    }

    private fun handleGoogleSignInSuccess(user: User.Authenticated?) {
        viewModelScope.launch {
            if (user != null) {
                // Firebase auth state will automatically sync via MainViewModel
                _eventChannel.send(AuthEvent.Success(UiText.Resource(Res.string.auth_success)))
            } else {
                shelfLifeLogger.warn("Google sign in succeeded but user is null")
                _eventChannel.send(AuthEvent.Error(UiText.Resource(Res.string.unknown_error_occurred)))
            }
        }
    }

    private fun sendError(error: Throwable) {
        viewModelScope.launch {
            if (error.message?.contains("A network error") == true) {
                _eventChannel.send(AuthEvent.Error(UiText.Resource(Res.string.internet_connection_unavailable)))
            } else if (error.message?.contains("Idtoken is null") == true) {
                _eventChannel.send(AuthEvent.Error(UiText.Resource(Res.string.sign_in_canceled)))
            } else if (error.message?.contains("The user account has been disabled") == true) {
                _eventChannel.send(AuthEvent.Error(UiText.Resource(Res.string.account_disabled)))
            } else {
                shelfLifeLogger.error(error.message ?: "Unknown error", error.cause)
                _eventChannel.send(AuthEvent.Error(UiText.Resource(Res.string.unknown_error_occurred)))
            }
        }
    }
}