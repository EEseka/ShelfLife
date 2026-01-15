package com.eeseka.shelflife.pantry.data

import com.eeseka.shelflife.pantry.domain.PantryRepository
import com.eeseka.shelflife.shared.domain.database.local.LocalPantryStorageService
import com.eeseka.shelflife.shared.domain.database.remote.RemotePantryStorageService
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.notification.NotificationService
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class OfflineFirstPantryRepository(
    private val localDataSource: LocalPantryStorageService,
    private val remoteDataSource: RemotePantryStorageService,
    private val notificationService: NotificationService,
    private val settingsService: SettingsService,
    private val logger: ShelfLifeLogger
) : PantryRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- READS ---
    override fun getAllPantryItems(): Flow<List<PantryItem>> {
        return localDataSource.getAllPantryItems()
    }

    override fun getPantryItemsByLocation(location: StorageLocation): Flow<List<PantryItem>> {
        return localDataSource.getPantryItemsByLocation(location)
    }

    override fun searchPantryItems(query: String): Flow<List<PantryItem>> {
        // SMART SEARCH LOGIC
        return if (isBarcode(query)) {
            flow {
                localDataSource.searchPantryItemByBarcode(query)
                    .onSuccess { pantryItem ->
                        pantryItem?.let { emit(listOf(it)) } ?: emit(emptyList())
                    }.onFailure { error ->
                        logger.error("Failed to search by barcode: $error")
                        emit(emptyList())
                    }
            }
        } else {
            localDataSource.searchPantryItemsByName(query)
        }
    }

    override fun searchPantryItemsByLocation(
        query: String,
        location: StorageLocation
    ): Flow<List<PantryItem>> {
        return if (isBarcode(query)) {
            flow {
                localDataSource.searchPantryItemByBarcodeAndLocation(query, location)
                    .onSuccess { pantryItem ->
                        pantryItem?.let { emit(listOf(it)) } ?: emit(emptyList())
                    }.onFailure { error ->
                        logger.error("Failed to search by barcode and location: $error")
                        emit(emptyList())
                    }
            }
        } else {
            localDataSource.searchPantryItemsByLocation(query, location)
        }
    }

    override fun getItemsExpiringSoon(withinDays: Int): Flow<List<PantryItem>> {
        return localDataSource.getItemsExpiringSoon(withinDays)
    }

    override suspend fun getPantryItemById(id: String): Result<PantryItem?, DataError> {
        return localDataSource.getPantryItem(id)
    }

    // --- WRITES ---
    override suspend fun createPantryItem(item: PantryItem): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        val localResult = localDataSource.upsertPantryItem(item, isSynced = false)

        //  Background Path: Try to Sync (Non-Blocking)
        if (localResult is Result.Success) {
            refreshNotifications()

            repositoryScope.launch {
                remoteDataSource.createPantryItem(userId, item)
                    .onSuccess {
                        localDataSource.upsertPantryItem(item, isSynced = true)
                    }
                    .onFailure {
                        // If it failed (Offline), WHO CARES?
                        // The UI already showed "Success".
                        // The item is safe locally as isSynced=false.
                        // Our 'syncRemotePantry' (Push-on-Open) will catch it later.
                        logger.info("Optimistic Sync: Immediate upload failed, queued for later.")
                    }
            }
        }

        return localResult
    }

    override suspend fun updatePantryItem(item: PantryItem): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        val localResult = localDataSource.upsertPantryItem(item, isSynced = false)

        if (localResult is Result.Success) {
            refreshNotifications()

            repositoryScope.launch {
                remoteDataSource.updatePantryItem(userId, item)
                    .onSuccess {
                        localDataSource.upsertPantryItem(item, isSynced = true)
                    }
                    .onFailure {
                        logger.info("Optimistic Sync: Immediate upload failed, queued for later.")
                    }
            }
        }
        return localResult
    }

    override suspend fun deletePantryItem(itemId: String): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        val localResult = localDataSource.deletePantryItem(itemId)

        // Fire-and-Forget to Firestore SDK
        // We trust Firestore's offline persistence queue to handle this eventually.
        if (localResult is Result.Success) {
            repositoryScope.launch {
                remoteDataSource.deletePantryItem(userId, itemId)
                    .onFailure {
                        logger.warn("Remote delete failed or pending (Offline): $itemId")
                    }
            }
        }
        return localResult
    }

    // --- SYNC ---
    override suspend fun syncRemotePantry(): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        // Two-way sync: remote → local, then local unsynced items → remote
        return remoteDataSource.getPantryItems(userId)
            .onSuccess { remoteItems ->
                localDataSource.syncPantryItems(remoteItems)
                    .onSuccess {
                        pushUnsyncedItemsToRemote(userId)
                        refreshNotifications()
                    }
            }
            .asEmptyResult()
    }

    // --- HELPERS ---
    private fun refreshNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            notificationService.refreshNotifications()
        }
    }

    private fun pushUnsyncedItemsToRemote(userId: String) = CoroutineScope(Dispatchers.IO).launch {
        localDataSource.getUnsyncedPantryItems()
            .onSuccess { unsyncedItems ->
                if (unsyncedItems.isNotEmpty()) {
                    logger.info("Found ${unsyncedItems.size} unsynced items, pushing to remote in parallel...")

                    supervisorScope {
                        // Process all unsynced items concurrently
                        unsyncedItems.map { item ->
                            async {
                                // Check if item exists on remote to determine create vs update
                                when (val result =
                                    remoteDataSource.getPantryItem(userId, item.id)) {
                                    // Item exists on server - update it
                                    is Result.Success -> {
                                        remoteDataSource.updatePantryItem(userId, item)
                                            .onSuccess {
                                                // Mark as synced locally
                                                localDataSource.upsertPantryItem(
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
                                            remoteDataSource.createPantryItem(userId, item)
                                                .onSuccess {
                                                    // Mark as synced locally
                                                    localDataSource.upsertPantryItem(
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
            logger.error("Attempted pantry operation without logged in user")
            return null
        }
        return user.id
    }

    private fun isBarcode(query: String): Boolean {
        return query.length > 6 && query.all { it.isDigit() }
    }
}