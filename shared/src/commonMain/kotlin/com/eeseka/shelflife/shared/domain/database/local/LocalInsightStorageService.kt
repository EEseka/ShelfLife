package com.eeseka.shelflife.shared.domain.database.local

import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface LocalInsightStorageService {
    suspend fun upsertInsightItem(
        insightItem: InsightItem,
        isSynced: Boolean
    ): EmptyResult<DataError.LocalStorage>

    fun getAllInsightItems(): Flow<List<InsightItem>>
    suspend fun deleteInsightItem(id: String): EmptyResult<DataError.LocalStorage>
    suspend fun deleteAllInsightItems(): EmptyResult<DataError.LocalStorage>
    suspend fun syncInsightItems(serverItems: List<InsightItem>): EmptyResult<DataError.LocalStorage>
    suspend fun getUnsyncedInsightItems(): Result<List<InsightItem>, DataError.LocalStorage>
}