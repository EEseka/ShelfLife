package com.eeseka.shelflife.shared.data.database.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eeseka.shelflife.shared.data.database.local.entities.PantryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryItemDao {

    /**
     * SINGLE ITEM OPERATIONS
     */

    @Upsert
    suspend fun upsertPantryItem(item: PantryItemEntity)

    @Upsert
    suspend fun upsertPantryItems(items: List<PantryItemEntity>)

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getPantryItemById(id: String): PantryItemEntity?

    @Query("SELECT * FROM pantry_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getPantryItemByBarcode(barcode: String): PantryItemEntity?

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deletePantryItemById(id: String)

    @Query("DELETE FROM pantry_items")
    suspend fun deleteAllPantryItems()

    /**
     * QUERIES (Read Operations)
     */

    // Sort by Expiry Date ASC (Soonest first)
    @Query("SELECT * FROM pantry_items ORDER BY expiryDate ASC")
    fun getAllPantryItems(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE storageLocation = :location ORDER BY expiryDate ASC")
    fun getPantryItemsByLocation(location: String): Flow<List<PantryItemEntity>>

    // Case-insensitive search on Name or Brand
    @Query(
        """
        SELECT * FROM pantry_items 
        WHERE name LIKE '%' || :query || '%' 
        OR brand LIKE '%' || :query || '%' 
        ORDER BY name ASC
    """
    )
    fun searchPantryItems(query: String): Flow<List<PantryItemEntity>>

    // Combined Search AND Filter
    @Query(
        """
        SELECT * FROM pantry_items 
        WHERE storageLocation = :location 
        AND (name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%')
        ORDER BY expiryDate ASC
    """
    )
    fun searchPantryItemsByLocation(query: String, location: String): Flow<List<PantryItemEntity>>

    // Get items expiring between now and target date (in epoch days/millis)
    @Query("SELECT * FROM pantry_items WHERE expiryDate BETWEEN :startMillis AND :endMillis ORDER BY expiryDate ASC")
    fun getItemsExpiringBetween(startMillis: Long, endMillis: Long): Flow<List<PantryItemEntity>>

    // Helpers
    @Query("SELECT id FROM pantry_items WHERE isSynced = 1")
    suspend fun getSyncedPantryItemIds(): List<String>

    @Query("SELECT * FROM pantry_items WHERE isSynced = 0")
    suspend fun getUnsyncedPantryItems(): List<PantryItemEntity>

    @Query("SELECT * FROM pantry_items WHERE id IN (:ids)")
    suspend fun getPantryItemsByIds(ids: List<String>): List<PantryItemEntity>

    @Query("DELETE FROM pantry_items WHERE id IN (:ids)")
    suspend fun deleteStalePantryItems(ids: List<String>)

    @Transaction
    suspend fun syncPantryItemsTransactional(serverItems: List<PantryItemEntity>) {

        // Get all IDs coming from the server
        val serverItemIds = serverItems.map { it.id }

        // Fetch what we currently have locally for these IDs
        val localItemsMap = getPantryItemsByIds(serverItemIds)
            .associateBy { it.id }

        // Filter: Decide which server items to keep
        val itemsToUpsert = serverItems.mapNotNull { serverItem ->
            val localItemEntity = localItemsMap[serverItem.id]

            // IF:
            // 1. It's a brand-new item (local == null) -> SAVE IT
            // 2. OR Server is newer than Local -> SAVE IT
            // 3. OR Local is already "Synced" (meaning it matches the old server state) -> SAVE IT
            val shouldOverwrite = localItemEntity == null ||
                serverItem.updatedAt > localItemEntity.updatedAt ||
                localItemEntity.isSynced

            if (shouldOverwrite) {
                // We are saving server data, so it is "Synced" by definition
                serverItem.copy(isSynced = true)
            } else {
                // STOP! We have an unsynced local edit that is newer.
                // Don't overwrite it with old server data.
                null
            }
        }

        // Batch Insert only the valid updates
        if (itemsToUpsert.isNotEmpty()) {
            upsertPantryItems(itemsToUpsert)
        }

        // --- CLEANUP ---

        // Remove items that were deleted on the server
        // BUT protect our "unsynced" local creations from being deleted
        val serverItemIdSet = serverItemIds.toSet()
        val localSyncedItemIds = getSyncedPantryItemIds() // Only look at synced items

        val staleItemIds = localSyncedItemIds - serverItemIdSet

        if (staleItemIds.isNotEmpty()) {
            deleteStalePantryItems(staleItemIds.toList())
        }
    }
}