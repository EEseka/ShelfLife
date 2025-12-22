package com.eeseka.shelflife.shared.data.auth

import com.eeseka.shelflife.shared.domain.auth.AuthService
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
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
            val (name, photo) = getBestProfileInfo(firebaseUser)

            User.Authenticated(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                fullName = name,
                profilePictureUrl = photo
            )
        }
    }

    override suspend fun signInAnonymously(): Result<User.Guest, DataError.Auth> {
        return safeFirebaseAuthCall(shelfLifeLogger) {
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

    override suspend fun deleteAccount(): EmptyResult<DataError.Auth> {
        val user = Firebase.auth.currentUser
            ?: return Result.Failure(DataError.Auth.UNAUTHORIZED)

        return safeFirebaseAuthCall(shelfLifeLogger) {
            user.delete()
        }
    }

    // I will need this after account upgrade as auth state doesn't automatically get it and simply reloading too won't help
    override suspend fun reloadAndGetUpgradedUser(): Result<User.Authenticated, DataError.Auth> {
        val firebaseUser =
            Firebase.auth.currentUser ?: return Result.Failure(DataError.Auth.UNAUTHORIZED)

        return try {
            firebaseUser.reload()
            val freshUser = Firebase.auth.currentUser
            if (freshUser != null && !freshUser.isAnonymous) {
                val (name, photo) = getBestProfileInfo(freshUser)
                val domainUser = User.Authenticated(
                    id = freshUser.uid,
                    email = freshUser.email ?: "",
                    fullName = name,
                    profilePictureUrl = photo
                )
                Result.Success(domainUser)
            } else {
                Result.Failure(DataError.Auth.UNKNOWN)
            }
        } catch (e: Exception) {
            shelfLifeLogger.warn("Failed to reload user: ${e.message}")
            Result.Failure(DataError.Auth.UNKNOWN)
        }
    }

    // After account upgrade (link with Google) only email is stored so we have to look through provider data to get name and pfp
    private fun getBestProfileInfo(user: FirebaseUser): Pair<String, String?> {
        // the main profile first
        var name = user.displayName
        var photo = user.photoURL

        // if missing, hunt through the providers (Google, Apple, etc.)
        if (name.isNullOrBlank()) {
            user.providerData.forEach { profile ->
                if (!profile.displayName.isNullOrBlank()) {
                    name = profile.displayName
                }
                if (photo == null && profile.photoURL != null) {
                    photo = profile.photoURL
                }
            }
        }

        return Pair(name ?: "", photo)
    }
}