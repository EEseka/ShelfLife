package com.eeseka.shelflife.shared.data.database.local

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<ShelfLifeDatabase>
}