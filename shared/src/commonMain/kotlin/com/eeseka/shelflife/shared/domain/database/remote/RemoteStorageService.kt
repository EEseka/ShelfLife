package com.eeseka.shelflife.shared.domain.database.remote

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result

interface RemoteStorageService {
    suspend fun createPantryItem(
        userId: String,
        pantryItem: PantryItem
    ): EmptyResult<DataError.Storage>

    suspend fun getPantryItems(userId: String): Result<List<PantryItem>, DataError.Storage>
    suspend fun getPantryItem(
        userId: String,
        pantryItemId: String
    ): Result<PantryItem, DataError.Storage>

    suspend fun updatePantryItem(
        userId: String,
        pantryItem: PantryItem
    ): EmptyResult<DataError.Storage>

    suspend fun deletePantryItem(
        userId: String,
        pantryItemId: String
    ): EmptyResult<DataError.Storage>
}