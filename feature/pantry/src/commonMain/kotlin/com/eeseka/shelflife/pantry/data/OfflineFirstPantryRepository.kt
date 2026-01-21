package com.eeseka.shelflife.pantry.data

import com.eeseka.shelflife.pantry.domain.PantryRepository
import com.eeseka.shelflife.shared.domain.database.local.LocalInsightStorageService
import com.eeseka.shelflife.shared.domain.database.local.LocalPantryStorageService
import com.eeseka.shelflife.shared.domain.database.remote.RemoteInsightStorageService
import com.eeseka.shelflife.shared.domain.database.remote.RemotePantryStorageService
import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.insight.InsightStatus
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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class OfflineFirstPantryRepository(
    private val localDataSource: LocalPantryStorageService,
    private val remoteDataSource: RemotePantryStorageService,
    private val insightLocalDataSource: LocalInsightStorageService,
    private val insightRemoteDataSource: RemoteInsightStorageService,
    private val notificationService: NotificationService,
    private val settingsService: SettingsService,
    private val logger: ShelfLifeLogger
) : PantryRepository {
    // Thread-safe map to track active background jobs
    private val activeJobs = mutableMapOf<String, Job>()
    private val activeJobsMutex = Mutex()
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

            val job = repositoryScope.launch(start = CoroutineStart.LAZY) {
                remoteDataSource.createPantryItem(userId, item)
                    .onSuccess { updatedRemoteItem ->
                        localDataSource.upsertPantryItem(updatedRemoteItem, isSynced = true)
                    }
                    .onFailure {
                        // If it failed (Offline), WHO CARES?
                        // The UI already showed "Success".
                        // The item is safe locally as isSynced=false.
                        // Our 'syncRemotePantry' (Push-on-Open) will catch it later.
                        logger.info("Optimistic Sync: Immediate upload failed, queued for later.")
                    }
            }

            // Track the job safely
            activeJobsMutex.withLock { activeJobs[item.id] = job }

            // Clean up when done
            job.invokeOnCompletion {
                repositoryScope.launch {
                    activeJobsMutex.withLock { activeJobs.remove(item.id) }
                }
            }

            job.start()
        }
        return localResult
    }

    override suspend fun updatePantryItem(item: PantryItem): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        val localResult = localDataSource.upsertPantryItem(item, isSynced = false)

        if (localResult is Result.Success) {
            refreshNotifications()

            val job = repositoryScope.launch(start = CoroutineStart.LAZY) {
                remoteDataSource.updatePantryItem(userId, item)
                    .onSuccess { updatedRemoteItem ->
                        localDataSource.upsertPantryItem(updatedRemoteItem, isSynced = true)
                    }
                    .onFailure {
                        logger.info("Optimistic Sync: Immediate upload failed, queued for later.")
                    }
            }

            activeJobsMutex.withLock { activeJobs[item.id] = job }


            job.invokeOnCompletion {
                repositoryScope.launch {
                    activeJobsMutex.withLock { activeJobs.remove(item.id) }
                }
            }

            job.start()
        }
        return localResult
    }

    override suspend fun deletePantryItem(itemId: String): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        val localResult = localDataSource.deletePantryItem(itemId)

        // Fire-and-Forget to Firestore SDK
        // We trust Firestore's offline persistence queue to handle this eventually.
        if (localResult is Result.Success) {
            refreshNotifications()

            repositoryScope.launch {
                remoteDataSource.deletePantryItem(userId, itemId)
                    .onFailure {
                        logger.warn("Remote delete failed or pending (Offline): $itemId")
                    }
            }
        }
        return localResult
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun movePantryItemToInsights(
        item: PantryItem,
        status: InsightStatus
    ): EmptyResult<DataError> {
        val userId = getUserIdOrError() ?: return Result.Failure(DataError.Auth.FORBIDDEN)

        val pendingJob = activeJobsMutex.withLock { activeJobs[item.id] }
        if (pendingJob != null) {
            logger.info("Race Condition Avoided: Waiting for pending sync on ${item.id}...")
            pendingJob.join() // Suspend here until the background upload finishes
        }

        val freshItemResult = localDataSource.getPantryItem(item.id)

        val sourceItem = if (freshItemResult is Result.Success && freshItemResult.data != null) {
            freshItemResult.data!!
        } else {
            item
        }

        val insightItem = InsightItem(
            id = sourceItem.id,
            name = sourceItem.name,
            imageUrl = sourceItem.imageUrl,
            quantity = sourceItem.quantity,
            quantityUnit = sourceItem.quantityUnit,
            status = status,
            actionDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            nutriScore = sourceItem.nutriScore,
            novaGroup = sourceItem.novaGroup,
            ecoScore = sourceItem.ecoScore
        )

        val insertResult = insightLocalDataSource.upsertInsightItem(insightItem, isSynced = false)
        if (insertResult is Result.Failure) return insertResult

        val deleteResult = localDataSource.deletePantryItem(item.id)

        if (deleteResult is Result.Failure) {
            logger.error("Move failed at delete step. Rolling back insight creation.")
            insightLocalDataSource.deleteInsightItem(insightItem.id)
            return deleteResult
        }

        // Background Sync (Parallel)
        repositoryScope.launch {
            val insightDeferred = async {
                insightRemoteDataSource.createInsightItem(userId, insightItem)
                    .onSuccess { updatedRemoteItem ->
                        insightLocalDataSource.upsertInsightItem(updatedRemoteItem, isSynced = true)
                    }
            }

            val deleteDeferred = async {
                // We are handing ownership of the image file to the Insight module.
                // The image remains in "pantry_images/..." bucket, but that's fine.
                remoteDataSource.deletePantryItem(userId, item.id, deleteImage = false)
            }

            awaitAll(insightDeferred, deleteDeferred)
        }

        return Result.Success(Unit)
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
        repositoryScope.launch {
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
                                            .onSuccess { updatedRemoteItem ->
                                                // Mark as synced locally
                                                localDataSource.upsertPantryItem(
                                                    updatedRemoteItem,
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
                                                .onSuccess { updatedRemoteItem ->
                                                    // Mark as synced locally
                                                    localDataSource.upsertPantryItem(
                                                        updatedRemoteItem,
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