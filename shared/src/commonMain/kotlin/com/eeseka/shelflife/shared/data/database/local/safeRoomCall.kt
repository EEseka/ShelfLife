package com.eeseka.shelflife.shared.data.database.local

import androidx.sqlite.SQLiteException
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.Result
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

suspend fun <T> safeRoomCall(
    logger: ShelfLifeLogger,
    action: suspend () -> T
): Result<T, DataError.LocalStorage> {
    return try {
        val result = action()
        Result.Success(result)
    } catch (e: SQLiteException) {
        logger.error("Database SQLite Error", e)
        val errorMessage = e.message?.lowercase() ?: ""

        val error = when {
            // "Disk full" errors often appear as "database or disk is full" in SQLite string
            errorMessage.contains("full") || errorMessage.contains("no space") ->
                DataError.LocalStorage.DISK_FULL

            errorMessage.contains("no such table") || errorMessage.contains("not found") ->
                DataError.LocalStorage.NOT_FOUND

            errorMessage.contains("unique") || errorMessage.contains("constraint") ->
                DataError.LocalStorage.UNKNOWN // Could be constraint violation

            else -> DataError.LocalStorage.UNKNOWN
        }
        Result.Failure(error)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        logger.error("Database Generic Error", e)
        Result.Failure(DataError.LocalStorage.UNKNOWN)
    }
}