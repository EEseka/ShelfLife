package com.eeseka.shelflife.shared.data.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insight_items")
data class InsightItemEntity(
    @PrimaryKey
    val id: String,

    val name: String,
    val imageUrl: String?,
    val quantity: Double,
    val quantityUnit: String,

    // --- The Analytics ---
    val status: String,
    val actionDate: Long,

    // --- SYNC METADATA ---
    val updatedAt: Long,
    val isSynced: Boolean = true,

    // --- Health Analytics (From PantryItem) ---
    val nutriScore: String?,
    val novaGroup: Int?,
    val ecoScore: String?
)