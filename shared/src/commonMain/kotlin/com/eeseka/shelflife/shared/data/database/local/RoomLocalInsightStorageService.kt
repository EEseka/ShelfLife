package com.eeseka.shelflife.shared.data.database.local

import com.eeseka.shelflife.shared.data.mappers.toDomain
import com.eeseka.shelflife.shared.data.mappers.toEntity
import com.eeseka.shelflife.shared.domain.database.local.LocalInsightStorageService
import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLocalInsightStorageService(
    shelfLifeDatabase: ShelfLifeDatabase,
    private val logger: ShelfLifeLogger
) : LocalInsightStorageService {
    private val insightItemDao = shelfLifeDatabase.insightItemDao

    override suspend fun upsertInsightItem(
        insightItem: InsightItem,
        isSynced: Boolean
    ): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            insightItemDao.upsertInsightItem(insightItem.toEntity().copy(isSynced = isSynced))
        }
    }

    override fun getAllInsightItems(): Flow<List<InsightItem>> {
        return insightItemDao.getAllInsightItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteInsightItem(id: String): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            insightItemDao.deleteInsightItemById(id)
        }
    }

    override suspend fun deleteAllInsightItems(): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            insightItemDao.deleteAllInsightItems()
        }
    }

    override suspend fun syncInsightItems(serverItems: List<InsightItem>): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            val serverEntities = serverItems.map { it.toEntity() }
            insightItemDao.syncInsightItemsTransactional(serverEntities)
        }
    }

    override suspend fun getUnsyncedInsightItems(): Result<List<InsightItem>, DataError.LocalStorage> {
        return safeRoomCall(logger) {
            insightItemDao.getUnsyncedInsightItems().map { it.toDomain() }
        }
    }
}