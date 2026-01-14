package com.eeseka.shelflife.shared.data.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey
    val id: String,

    // Identity
    val barcode: String,
    val name: String,
    val brand: String?,

    // Images (Firebase Storage URLs or local paths)
    val imageUrl: String?,
    val thumbnailUrl: String?,

    // Quantity
    val quantity: Double,
    val quantityUnit: String,
    val packagingSize: String?,

    // Dates (stored as epoch millis)
    val expiryDate: Long,
    val purchaseDate: Long,
    val openDate: Long?,

    // Storage location (enum as String)
    val storageLocation: String,   // "PANTRY", "FRIDGE", "FREEZER"

    // Metadata for offline-first sync
    val updatedAt: Long,
    // Used for conflict resolution in sync operations
    // When syncing, if server item's updatedAt > local item's updatedAt, server wins (update local)
    // This prevents lost updates when multiple devices modify same item offline
    val isSynced: Boolean = true,

    // Health & Insights
    val nutriScore: String?,
    val novaGroup: Int?,
    val ecoScore: String?,
    val allergens: String,         // Comma-separated list
    val labels: String,            // Comma-separated list

    // Nutrition (per 100g/ml)
    val caloriesPer100g: Int?,
    val sugarPer100g: Double?,
    val fatPer100g: Double?,
    val proteinPer100g: Double?
)