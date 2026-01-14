package com.eeseka.shelflife.shared.data.database.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<ShelfLifeDatabase> {
        val dbFile = context.applicationContext.getDatabasePath(ShelfLifeDatabase.DB_NAME)

        return Room.databaseBuilder(
            context.applicationContext,
            dbFile.absolutePath
        )
    }
}