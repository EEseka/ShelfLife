package com.eeseka.shelflife.shared.data.database.remote

import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.Result
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.code
import dev.gitlive.firebase.storage.FirebaseStorageException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException

suspend fun <T> safeFirebaseFirestoreCall(
    logger: ShelfLifeLogger,
    action: suspend () -> T
): Result<T, DataError.Storage> {
    return try {
        val result = action()
        Result.Success(result)
    } catch (e: FirebaseFirestoreException) {
        val error = when (e.code) {
            FirestoreExceptionCode.PERMISSION_DENIED -> DataError.Storage.PERMISSION_DENIED
            FirestoreExceptionCode.UNAUTHENTICATED -> DataError.Storage.PERMISSION_DENIED
            FirestoreExceptionCode.NOT_FOUND -> DataError.Storage.NOT_FOUND
            FirestoreExceptionCode.ALREADY_EXISTS -> DataError.Storage.CONFLICT
            FirestoreExceptionCode.RESOURCE_EXHAUSTED -> DataError.Storage.QUOTA_EXCEEDED
            FirestoreExceptionCode.DEADLINE_EXCEEDED -> DataError.Storage.REQUEST_TIMEOUT
            FirestoreExceptionCode.UNAVAILABLE -> DataError.Storage.NO_INTERNET
            FirestoreExceptionCode.INVALID_ARGUMENT -> DataError.Storage.BAD_REQUEST
            FirestoreExceptionCode.ABORTED -> DataError.Storage.SERVER_ERROR
            FirestoreExceptionCode.INTERNAL -> DataError.Storage.SERVER_ERROR
            FirestoreExceptionCode.DATA_LOSS -> DataError.Storage.SERVER_ERROR
            FirestoreExceptionCode.CANCELLED -> DataError.Storage.REQUEST_TIMEOUT
            FirestoreExceptionCode.FAILED_PRECONDITION -> DataError.Storage.BAD_REQUEST
            FirestoreExceptionCode.OUT_OF_RANGE -> DataError.Storage.BAD_REQUEST
            FirestoreExceptionCode.UNIMPLEMENTED -> DataError.Storage.SERVER_ERROR
            else -> DataError.Storage.UNKNOWN
        }
        Result.Failure(error)
    } catch (e: FirebaseStorageException) {
        // Parse Storage-specific errors from message
        // Note: Unlike Firestore, Storage doesn't expose a typed 'code' enum in GitLive SDK
        // Firebase Storage error codes: https://firebase.google.com/docs/storage/web/handle-errors
        val errorMessage = e.message?.lowercase() ?: ""
        val error = when {
            // Storage Authentication/Permission errors
            errorMessage.contains("unauthorized") ||
                    errorMessage.contains("unauthenticated") ||
                    errorMessage.contains("permission-denied") ->
                DataError.Storage.PERMISSION_DENIED

            // Storage Quota errors
            errorMessage.contains("quota") ||
                    errorMessage.contains("quota-exceeded") ->
                DataError.Storage.QUOTA_EXCEEDED

            // Rate limiting
            errorMessage.contains("retry-limit") ->
                DataError.Storage.TOO_MANY_REQUESTS

            // Not found
            errorMessage.contains("object-not-found") ||
                    errorMessage.contains("not-found") ->
                DataError.Storage.NOT_FOUND

            // Cancelled/Timeout
            errorMessage.contains("canceled") ||
                    errorMessage.contains("cancelled") ->
                DataError.Storage.REQUEST_TIMEOUT

            // Invalid arguments
            errorMessage.contains("invalid-argument") ->
                DataError.Storage.BAD_REQUEST

            // Server errors
            errorMessage.contains("server") ||
                    errorMessage.contains("internal") ->
                DataError.Storage.SERVER_ERROR

            else -> DataError.Storage.UNKNOWN
        }
        Result.Failure(error)
    } catch (e: SerializationException) {
        logger.error("Serialization error", e)
        Result.Failure(DataError.Storage.SERIALIZATION)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        logger.error("Generic Exception error", e)

        // Map common errors including custom exceptions
        val errorMessage = e.message?.lowercase() ?: ""
        val error = when {
            // Custom exceptions from our code
            errorMessage.contains("already exists") ->
                DataError.Storage.CONFLICT

            errorMessage.contains("not found") ->
                DataError.Storage.NOT_FOUND
            // Network errors
            errorMessage.contains("timeout") ->
                DataError.Storage.REQUEST_TIMEOUT

            errorMessage.contains("network") || errorMessage.contains("connection") ->
                DataError.Storage.NO_INTERNET

            else -> DataError.Storage.UNKNOWN
        }
        Result.Failure(error)
    }
}
