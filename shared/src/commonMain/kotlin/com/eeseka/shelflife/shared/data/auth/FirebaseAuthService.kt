package com.eeseka.shelflife.shared.data.auth

import com.eeseka.shelflife.shared.data.networking.safeFirebaseCall
import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthService(
    private val shelfLifeLogger: ShelfLifeLogger
) : AuthService {

    override suspend fun validateSession() {
        val user = Firebase.auth.currentUser ?: return // Already logged out, nothing to do.

        try {
            // This Forces a network call to Firebase to check if the account still exists.
            user.reload()
        } catch (e: Exception) {
            // If we get HERE, it means the token is invalid or user is deleted on server.
            shelfLifeLogger.warn("Session is invalid (User deleted?): ${e.message}")

            // Force local cleanup immediately
            signOut()
        }
    }

    override val authState: Flow<User?> = Firebase.auth.authStateChanged.map { firebaseUser ->
        if (firebaseUser == null) {
            null
        } else if (firebaseUser.isAnonymous) {
            User.Guest(id = firebaseUser.uid)
        } else {
            User.Authenticated(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                fullName = firebaseUser.displayName ?: "",
                profilePictureUrl = firebaseUser.photoURL
            )
        }
    }

    override suspend fun signInAnonymously(): Result<User.Guest, DataError.Remote> {
        return safeFirebaseCall {
            val result = Firebase.auth.signInAnonymously()
            User.Guest(id = result.user?.uid ?: "")
        }
    }

    override suspend fun signOut(): EmptyResult<DataError.Local> {
        return try {
            // Sign out is rarely a "Remote" error. It mainly clears local storage.
            Firebase.auth.signOut()
            Result.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            shelfLifeLogger.warn("Error signing out: ${e.message}")
            Result.Failure(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun deleteAccount(): EmptyResult<DataError.Remote> {
        val user = Firebase.auth.currentUser
            ?: return Result.Failure(DataError.Remote.UNAUTHORIZED)

        return safeFirebaseCall {
            user.delete()
        }
    }
}