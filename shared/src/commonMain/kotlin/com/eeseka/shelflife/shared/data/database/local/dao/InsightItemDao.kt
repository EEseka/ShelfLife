package com.eeseka.shelflife.shared.data.database.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.eeseka.shelflife.shared.data.database.local.entities.InsightItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightItemDao {

    /**
     * SINGLE ITEM OPERATIONS
     */

    @Upsert
    suspend fun upsertInsightItem(item: InsightItemEntity)

    @Query("DELETE FROM insight_items WHERE id = :id")
    suspend fun deleteInsightItemById(id: String)

    @Query("DELETE FROM insight_items")
    suspend fun deleteAllInsightItems()

    /**
     * QUERIES (Read Operations)
     */

    // Sort by Expiry Date ASC (Soonest first)
    @Query("SELECT * FROM insight_items ORDER BY actionDate DESC")
    fun getAllInsightItems(): Flow<List<InsightItemEntity>>

    // Helpers
    @Upsert
    suspend fun upsertInsightItems(items: List<InsightItemEntity>)

    @Query("SELECT id FROM insight_items WHERE isSynced = 1")
    suspend fun getSyncedInsightItemIds(): List<String>

    @Query("SELECT * FROM insight_items WHERE isSynced = 0")
    suspend fun getUnsyncedInsightItems(): List<InsightItemEntity>

    @Query("SELECT * FROM insight_items WHERE id IN (:ids)")
    suspend fun getInsightItemsByIds(ids: List<String>): List<InsightItemEntity>

    @Query("DELETE FROM insight_items WHERE id IN (:ids)")
    suspend fun deleteStaleInsightItems(ids: List<String>)

    @Transaction
    suspend fun syncInsightItemsTransactional(serverItems: List<InsightItemEntity>) {

        // Get all IDs coming from the server
        val serverItemIds = serverItems.map { it.id }

        // Fetch what we currently have locally for these IDs
        val localItemsMap = getInsightItemsByIds(serverItemIds)
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
            upsertInsightItems(itemsToUpsert)
        }

        // --- CLEANUP ---

        // Remove items that were deleted on the server
        // BUT protect our "unsynced" local creations from being deleted
        val serverItemIdSet = serverItemIds.toSet()
        val localSyncedItemIds = getSyncedInsightItemIds() // Only look at synced items

        val staleItemIds = localSyncedItemIds - serverItemIdSet

        if (staleItemIds.isNotEmpty()) {
            deleteStaleInsightItems(staleItemIds.toList())
        }
    }
}