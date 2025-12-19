package com.eeseka.shelflife.shared.domain.auth

import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthService {
    suspend fun validateSession()
    val authState: Flow<User?>
    suspend fun signInAnonymously(): Result<User.Guest, DataError.Remote>
    suspend fun signOut(): EmptyResult<DataError.Local>
    suspend fun deleteAccount(): EmptyResult<DataError.Remote>
    suspend fun reloadAndGetUpgradedUser(): Result<User.Authenticated, DataError.Remote>
}