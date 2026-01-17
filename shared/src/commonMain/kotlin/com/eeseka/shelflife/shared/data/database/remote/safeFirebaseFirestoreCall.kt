package com.eeseka.shelflife.shared.data.database.remote

import com.eeseka.shelflife.shared.data.database.util.FirebaseFirestoreConflictException
import com.eeseka.shelflife.shared.data.database.util.FirebaseFirestoreNotFoundException
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
): Result<T, DataError.RemoteStorage> {
    return try {
        val result = action()
        Result.Success(result)
        // Specific Custom Exceptions
    } catch (e: FirebaseFirestoreConflictException) {
        logger.warn("Conflict: ${e.message}")
        Result.Failure(DataError.RemoteStorage.CONFLICT)
    } catch (e: FirebaseFirestoreNotFoundException) {
        logger.warn("Not Found: ${e.message}")
        Result.Failure(DataError.RemoteStorage.NOT_FOUND)
        // Non-Custom Exceptions
    } catch (e: FirebaseFirestoreException) {
        val error = when (e.code) {
            FirestoreExceptionCode.PERMISSION_DENIED -> DataError.RemoteStorage.PERMISSION_DENIED
            FirestoreExceptionCode.UNAUTHENTICATED -> DataError.RemoteStorage.PERMISSION_DENIED
            FirestoreExceptionCode.NOT_FOUND -> DataError.RemoteStorage.NOT_FOUND
            FirestoreExceptionCode.ALREADY_EXISTS -> DataError.RemoteStorage.CONFLICT
            FirestoreExceptionCode.RESOURCE_EXHAUSTED -> DataError.RemoteStorage.QUOTA_EXCEEDED
            FirestoreExceptionCode.DEADLINE_EXCEEDED -> DataError.RemoteStorage.REQUEST_TIMEOUT
            FirestoreExceptionCode.UNAVAILABLE -> DataError.RemoteStorage.NO_INTERNET
            FirestoreExceptionCode.INVALID_ARGUMENT -> DataError.RemoteStorage.BAD_REQUEST
            FirestoreExceptionCode.ABORTED -> DataError.RemoteStorage.SERVER_ERROR
            FirestoreExceptionCode.INTERNAL -> DataError.RemoteStorage.SERVER_ERROR
            FirestoreExceptionCode.DATA_LOSS -> DataError.RemoteStorage.SERVER_ERROR
            FirestoreExceptionCode.CANCELLED -> DataError.RemoteStorage.REQUEST_TIMEOUT
            FirestoreExceptionCode.FAILED_PRECONDITION -> DataError.RemoteStorage.BAD_REQUEST
            FirestoreExceptionCode.OUT_OF_RANGE -> DataError.RemoteStorage.BAD_REQUEST
            FirestoreExceptionCode.UNIMPLEMENTED -> DataError.RemoteStorage.SERVER_ERROR
            else -> DataError.RemoteStorage.UNKNOWN
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
                DataError.RemoteStorage.PERMISSION_DENIED

            // Storage Quota errors
            errorMessage.contains("quota") ||
                    errorMessage.contains("quota-exceeded") ->
                DataError.RemoteStorage.QUOTA_EXCEEDED

            // Rate limiting
            errorMessage.contains("retry-limit") ->
                DataError.RemoteStorage.TOO_MANY_REQUESTS

            // Not found
            errorMessage.contains("object-not-found") ||
                    errorMessage.contains("not-found") ->
                DataError.RemoteStorage.NOT_FOUND

            // Cancelled/Timeout
            errorMessage.contains("canceled") ||
                    errorMessage.contains("cancelled") ->
                DataError.RemoteStorage.REQUEST_TIMEOUT

            // Invalid arguments
            errorMessage.contains("invalid-argument") ->
                DataError.RemoteStorage.BAD_REQUEST

            // Server errors
            errorMessage.contains("server") ||
                    errorMessage.contains("internal") ->
                DataError.RemoteStorage.SERVER_ERROR

            else -> DataError.RemoteStorage.UNKNOWN
        }
        Result.Failure(error)
    } catch (e: SerializationException) {
        logger.error("Serialization error", e)
        Result.Failure(DataError.RemoteStorage.SERIALIZATION)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        logger.error("Generic Exception error", e)

        val errorMessage = e.message?.lowercase() ?: ""
        val error = when {
            // Network errors are often generic IOExceptions
            errorMessage.contains("timeout") ||
                    errorMessage.contains("deadline_exceeded") ->
                DataError.RemoteStorage.REQUEST_TIMEOUT

            errorMessage.contains("network") ||
                    errorMessage.contains("connection") ||
                    errorMessage.contains("offline") ||
                    errorMessage.contains("unavailable") ->
                DataError.RemoteStorage.NO_INTERNET

            else -> DataError.RemoteStorage.UNKNOWN
        }
        Result.Failure(error)
    }
}
