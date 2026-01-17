package com.eeseka.shelflife.shared.data.database.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.eeseka.shelflife.shared.data.database.local.dao.InsightItemDao
import com.eeseka.shelflife.shared.data.database.local.dao.PantryItemDao
import com.eeseka.shelflife.shared.data.database.local.entities.InsightItemEntity
import com.eeseka.shelflife.shared.data.database.local.entities.PantryItemEntity

@Database(entities = [PantryItemEntity::class, InsightItemEntity::class], version = 1)
@ConstructedBy(ShelfLifeDatabaseConstructor::class)
abstract class ShelfLifeDatabase : RoomDatabase() {
    abstract val pantryItemDao: PantryItemDao
    abstract val insightItemDao: InsightItemDao

    companion object {
        const val DB_NAME = "shelflife.db"
    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object ShelfLifeDatabaseConstructor : RoomDatabaseConstructor<ShelfLifeDatabase> {
    override fun initialize(): ShelfLifeDatabase
}