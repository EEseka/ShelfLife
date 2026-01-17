package com.eeseka.shelflife.insights.domain

import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface InsightRepository {
    fun getAllInsightItems(): Flow<List<InsightItem>>
    suspend fun deleteAllInsightItems(): EmptyResult<DataError>
    suspend fun syncRemoteInsight(): EmptyResult<DataError>
}