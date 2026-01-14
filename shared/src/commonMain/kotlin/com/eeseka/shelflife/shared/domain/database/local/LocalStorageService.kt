package com.eeseka.shelflife.shared.domain.database.local

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface LocalStorageService {
    suspend fun upsertPantryItem(
        pantryItem: PantryItem,
        isSynced: Boolean
    ): EmptyResult<DataError.LocalStorage>

    suspend fun getPantryItem(pantryItemId: String): Result<PantryItem?, DataError.LocalStorage>

    fun getAllPantryItems(): Flow<List<PantryItem>>

    fun getPantryItemsByLocation(location: StorageLocation): Flow<List<PantryItem>>

    fun searchPantryItemsByName(query: String): Flow<List<PantryItem>>

    suspend fun searchPantryItemByBarcode(barcode: String): Result<PantryItem?, DataError.LocalStorage>

    fun searchPantryItemsByLocation(
        query: String,
        location: StorageLocation
    ): Flow<List<PantryItem>>

    fun getItemsExpiringSoon(withinDays: Int): Flow<List<PantryItem>>

    suspend fun deletePantryItem(pantryItemId: String): EmptyResult<DataError.LocalStorage>

    suspend fun deleteAllPantryItems(): EmptyResult<DataError.LocalStorage>

    suspend fun syncPantryItems(serverItems: List<PantryItem>): EmptyResult<DataError.LocalStorage>

    suspend fun getUnsyncedPantryItems(): Result<List<PantryItem>, DataError.LocalStorage>
}