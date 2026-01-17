package com.eeseka.shelflife.shared.domain.database.remote

import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result

interface RemoteInsightStorageService {
    suspend fun createInsightItem(
        userId: String,
        insightItem: InsightItem
    ): EmptyResult<DataError.RemoteStorage>

    suspend fun getInsightItems(userId: String): Result<List<InsightItem>, DataError.RemoteStorage>

    suspend fun getInsightItem(
        userId: String,
        insightItemId: String
    ): Result<InsightItem, DataError.RemoteStorage>

    suspend fun updateInsightItem(
        userId: String,
        insightItem: InsightItem
    ): EmptyResult<DataError.RemoteStorage>

    suspend fun deleteInsightItem(
        userId: String,
        insightItemId: String
    ): EmptyResult<DataError.RemoteStorage>

    suspend fun deleteAllInsightItems(
        userId: String
    ): EmptyResult<DataError.RemoteStorage>
}