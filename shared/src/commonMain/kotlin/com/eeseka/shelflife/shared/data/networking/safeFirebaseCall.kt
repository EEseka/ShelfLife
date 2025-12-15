package com.eeseka.shelflife.shared.data.networking

import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.Result
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

suspend fun <T> safeFirebaseCall(
    action: suspend () -> T
): Result<T, DataError.Remote> {
    return try {
        val result = action()
        Result.Success(result)
    } catch (e: FirebaseAuthInvalidUserException) {
        // User account disabled, deleted, or token expired
        Result.Failure(DataError.Remote.UNAUTHORIZED)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        // Wrong password or malformed token
        Result.Failure(DataError.Remote.UNAUTHORIZED)
    } catch (e: FirebaseAuthRecentLoginRequiredException) {
        // Sensitive operation (like delete account) requires re-login
        // Mapping to FORBIDDEN hints the UI to ask for credentials again
        Result.Failure(DataError.Remote.FORBIDDEN)
    } catch (e: FirebaseAuthUserCollisionException) {
        // Account already exists
        Result.Failure(DataError.Remote.CONFLICT)
    } catch (e: FirebaseAuthException) {
        // Generic Firebase error
        e.printStackTrace()
        Result.Failure(DataError.Remote.UNKNOWN)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        e.printStackTrace()
        // Map unknown/network errors
        Result.Failure(DataError.Remote.UNKNOWN)
    }
}