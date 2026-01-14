package com.eeseka.shelflife.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PantryItemSerializable(
    val id: String,
    val barcode: String,
    val name: String,
    val brand: String? = null,
    
    // Image URLs (already uploaded to Firebase Storage)
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
    
    // Quantity
    val quantity: Double = 1.0,
    val quantityUnit: String = "pcs",
    val packagingSize: String? = null,
    
    // Dates stored as ISO-8601 strings for Firestore compatibility
    val expiryDate: String, // LocalDate.toString() -> "2024-12-25"
    val purchaseDate: String,
    val openDate: String? = null,
    
    // Storage location as string
    val storageLocation: String = "PANTRY", // "PANTRY", "FRIDGE", or "FREEZER"

    // METADATA (CRITICAL FOR OFFLINE-FIRST CONFLICT RESOLUTION)
    val updatedAt: Long,

    // Health & Insights
    val nutriScore: String? = null,
    val novaGroup: Int? = null,
    val ecoScore: String? = null,
    val allergens: List<String> = emptyList(),
    val labels: List<String> = emptyList(),
    
    // Nutrition
    val caloriesPer100g: Int? = null,
    val sugarPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val proteinPer100g: Double? = null
)
