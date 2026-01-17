package com.eeseka.shelflife.pantry.domain

import com.eeseka.shelflife.shared.domain.insight.InsightStatus
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface PantryRepository {
    // --- READS ---
    fun getAllPantryItems(): Flow<List<PantryItem>>

    fun getPantryItemsByLocation(location: StorageLocation): Flow<List<PantryItem>>

    /**
     * Smart Search:
     * - If query is numeric (barcode), searches by barcode.
     * - Otherwise searches by name.
     */
    fun searchPantryItems(query: String): Flow<List<PantryItem>>

    fun searchPantryItemsByLocation(
        query: String,
        location: StorageLocation
    ): Flow<List<PantryItem>>

    fun getItemsExpiringSoon(withinDays: Int): Flow<List<PantryItem>>

    suspend fun getPantryItemById(id: String): Result<PantryItem?, DataError>

    // --- WRITES ---

    suspend fun createPantryItem(item: PantryItem): EmptyResult<DataError>

    suspend fun updatePantryItem(item: PantryItem): EmptyResult<DataError>

    suspend fun deletePantryItem(itemId: String): EmptyResult<DataError>

    suspend fun movePantryItemToInsights(
        item: PantryItem,
        status: InsightStatus
    ): EmptyResult<DataError>

    // --- SYNC ---
    suspend fun syncRemotePantry(): EmptyResult<DataError>
}