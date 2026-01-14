package com.eeseka.shelflife.shared.data.database.local

import com.eeseka.shelflife.shared.data.mappers.toDomain
import com.eeseka.shelflife.shared.data.mappers.toEntity
import com.eeseka.shelflife.shared.domain.database.local.LocalStorageService
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class RoomLocalStorageService(
    pantryItemDatabase: ShelfLifeDatabase,
    private val logger: ShelfLifeLogger,
) : LocalStorageService {
    private val pantryItemDao = pantryItemDatabase.pantryItemDao

    @OptIn(ExperimentalTime::class)
    override suspend fun upsertPantryItem(
        pantryItem: PantryItem,
        isSynced: Boolean
    ): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            pantryItemDao.upsertPantryItem(pantryItem.toEntity().copy(isSynced = isSynced))
        }
    }

    override suspend fun getPantryItem(pantryItemId: String): Result<PantryItem?, DataError.LocalStorage> {
        return safeRoomCall(logger) {
            pantryItemDao.getPantryItemById(pantryItemId)?.toDomain()
        }
    }

    override fun getAllPantryItems(): Flow<List<PantryItem>> {
        return pantryItemDao.getAllPantryItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPantryItemsByLocation(location: StorageLocation): Flow<List<PantryItem>> {
        return pantryItemDao.getPantryItemsByLocation(location.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchPantryItemsByName(query: String): Flow<List<PantryItem>> {
        return pantryItemDao.searchPantryItems(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun searchPantryItemByBarcode(barcode: String): Result<PantryItem?, DataError.LocalStorage> {
        return safeRoomCall(logger) {
            pantryItemDao.getPantryItemByBarcode(barcode)?.toDomain()
        }
    }

    override fun searchPantryItemsByLocation(
        query: String,
        location: StorageLocation
    ): Flow<List<PantryItem>> {
        return pantryItemDao.searchPantryItemsByLocation(query, location.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun getItemsExpiringSoon(withinDays: Int): Flow<List<PantryItem>> {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        val startEpochDay = today.toEpochDays()
        val endEpochDay = today.plus(DatePeriod(days = withinDays)).toEpochDays()

        return pantryItemDao.getItemsExpiringBetween(startEpochDay, endEpochDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deletePantryItem(pantryItemId: String): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            pantryItemDao.deletePantryItemById(pantryItemId)
        }
    }

    override suspend fun deleteAllPantryItems(): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            pantryItemDao.deleteAllPantryItems()
        }
    }

    override suspend fun syncPantryItems(serverItems: List<PantryItem>): EmptyResult<DataError.LocalStorage> {
        return safeRoomCall(logger) {
            val serverEntities = serverItems.map { it.toEntity() }
            pantryItemDao.syncPantryItemsTransactional(serverEntities)
        }
    }

    override suspend fun getUnsyncedPantryItems(): Result<List<PantryItem>, DataError.LocalStorage> {
        return safeRoomCall(logger) {
            pantryItemDao.getUnsyncedPantryItems().map { it.toDomain() }
        }
    }
}