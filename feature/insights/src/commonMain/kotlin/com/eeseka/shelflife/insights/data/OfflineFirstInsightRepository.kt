package com.eeseka.shelflife.insights.data

import com.eeseka.shelflife.insights.domain.InsightRepository
import com.eeseka.shelflife.shared.domain.database.local.LocalInsightStorageService
import com.eeseka.shelflife.shared.domain.database.remote.RemoteInsightStorageService
import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import com.eeseka.shelflife.shared.domain.util.asEmptyResult
import com.eeseka.shelflife.shared.domain.util.onFailure
import com.eeseka.shelflife.shared.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class OfflineFirstInsightRepository(
    private val localDataSource: LocalInsightStorageService,
    private val remoteDataSource: RemoteInsightStorageService,
    private val settingsService: SettingsService,
    private val logger: ShelfLifeLogger
) : InsightRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllInsightItems(): Flow<List<InsightItem>> {
        return localDataSource.getAllInsightItems()
    }

    override suspend fun deleteAllInsightItems(): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        val localResult = localDataSource.deleteAllInsightItems()

        // Fire-and-Forget to Firestore SDK
        // We trust Firestore's offline persistence queue to handle this eventually.
        if (localResult is Result.Success) {
            repositoryScope.launch {
                remoteDataSource.deleteAllInsightItems(userId)
                    .onFailure {
                        logger.warn("Remote delete failed or pending (Offline)")
                    }
            }
        }
        return localResult
    }

    override suspend fun syncRemoteInsight(): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        // Two-way sync: remote → local, then local unsynced items → remote
        return remoteDataSource.getInsightItems(userId)
            .onSuccess { remoteItems ->
                localDataSource.syncInsightItems(remoteItems)
                    .onSuccess {
                        pushUnsyncedItemsToRemote(userId)
                    }
            }
            .asEmptyResult()
    }

    // --- HELPERS ---
    private fun pushUnsyncedItemsToRemote(userId: String) = CoroutineScope(Dispatchers.IO).launch {
        localDataSource.getUnsyncedInsightItems()
            .onSuccess { unsyncedItems ->
                if (unsyncedItems.isNotEmpty()) {
                    logger.info("Found ${unsyncedItems.size} unsynced items, pushing to remote in parallel...")

                    supervisorScope {
                        // Process all unsynced items concurrently
                        unsyncedItems.map { item ->
                            async {
                                // Check if item exists on remote to determine create vs update
                                when (val result =
                                    remoteDataSource.getInsightItem(userId, item.id)) {
                                    // Item exists on server - update it
                                    is Result.Success -> {
                                        remoteDataSource.updateInsightItem(userId, item)
                                            .onSuccess {
                                                // Mark as synced locally
                                                localDataSource.upsertInsightItem(
                                                    item,
                                                    isSynced = true
                                                )
                                                logger.info("Successfully updated and synced item: ${item.id}")
                                            }
                                            .onFailure { error ->
                                                logger.error("Failed to update item ${item.id} on remote: $error")
                                                // Leave as isSynced=false for next retry
                                            }
                                    }

                                    // Item doesn't exist (NOT_FOUND only) - create new
                                    is Result.Failure -> {
                                        if (result.error == DataError.RemoteStorage.NOT_FOUND) {
                                            remoteDataSource.createInsightItem(userId, item)
                                                .onSuccess {
                                                    // Mark as synced locally
                                                    localDataSource.upsertInsightItem(
                                                        item,
                                                        isSynced = true
                                                    )
                                                    logger.info("Successfully created and synced item: ${item.id}")
                                                }
                                                .onFailure { error ->
                                                    logger.error("Failed to create item ${item.id} on remote: $error")
                                                    // Leave as isSynced=false for next retry
                                                }
                                        } else {
                                            // Other errors (network, permission, etc.) - skip this item
                                            // It will be retried on next sync
                                            logger.warn("Skipping item ${item.id} due to error: ${result.error}")
                                        }
                                    }
                                }
                            }
                        }.awaitAll() // Wait for all items to complete
                    }
                }
            }
            .onFailure { error ->
                logger.error("Failed to retrieve unsynced items: $error")
            }
    }

    private suspend fun getUserIdOrError(): String? {
        val user = settingsService.cachedUser.first()
        if (user == null) {
            logger.error("Attempted insight operation without logged in user")
            return null
        }
        return user.id
    }
}