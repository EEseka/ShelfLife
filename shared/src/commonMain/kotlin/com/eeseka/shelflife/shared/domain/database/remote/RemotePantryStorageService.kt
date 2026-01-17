package com.eeseka.shelflife.shared.domain.database.remote

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result

interface RemotePantryStorageService {
    suspend fun createPantryItem(
        userId: String,
        pantryItem: PantryItem
    ): EmptyResult<DataError.RemoteStorage>

    suspend fun getPantryItems(userId: String): Result<List<PantryItem>, DataError.RemoteStorage>
    suspend fun getPantryItem(
        userId: String,
        pantryItemId: String
    ): Result<PantryItem, DataError.RemoteStorage>

    suspend fun updatePantryItem(
        userId: String,
        pantryItem: PantryItem
    ): EmptyResult<DataError.RemoteStorage>

    suspend fun deletePantryItem(
        userId: String,
        pantryItemId: String,
        deleteImage: Boolean = true
    ): EmptyResult<DataError.RemoteStorage>
}