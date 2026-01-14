package com.eeseka.shelflife.shared.data.auth

import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.Result
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

suspend fun <T> safeFirebaseAuthCall(
    logger: ShelfLifeLogger,
    action: suspend () -> T
): Result<T, DataError.Auth> {
    return try {
        val result = action()
        Result.Success(result)
    } catch (e: FirebaseAuthInvalidUserException) {
        // User account disabled, deleted, or token expired
        Result.Failure(DataError.Auth.UNAUTHORIZED)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        // Wrong password or malformed token
        Result.Failure(DataError.Auth.UNAUTHORIZED)
    } catch (e: FirebaseAuthRecentLoginRequiredException) {
        // Sensitive operation (like delete account) requires re-login
        // Mapping to FORBIDDEN hints the UI to ask for credentials again
        Result.Failure(DataError.Auth.FORBIDDEN)
    } catch (e: FirebaseAuthUserCollisionException) {
        // Account already exists
        Result.Failure(DataError.Auth.CONFLICT)
    } catch (e: FirebaseAuthException) {
        // Generic Firebase error
        logger.error("Firebase Auth Exception error", e)
        Result.Failure(DataError.Auth.UNKNOWN)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        logger.error("Generic Exception error", e)

        val errorMessage = e.message?.lowercase() ?: ""
        val error = when {
            errorMessage.contains("network") ||
                    errorMessage.contains("connection") ||
                    errorMessage.contains("offline") ||
                    errorMessage.contains("timeout") -> DataError.Auth.NO_INTERNET

            else -> DataError.Auth.UNKNOWN
        }
        Result.Failure(error)
    }
}