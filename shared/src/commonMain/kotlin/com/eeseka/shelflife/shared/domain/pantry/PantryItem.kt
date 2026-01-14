package com.eeseka.shelflife.shared.domain.pantry

import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class PantryItem(
    // --- IDENTITY (Local & API) ---
    val id: String, // UUID for local database uniqueness
    val barcode: String, // The lookup key for Open Food Facts (OFF)
    val name: String, // Product Name
    val brand: String? = null, // e.g., "Heinz", "Kellogg's"

    // --- IMAGES (From OFF) ---
    val imageUrl: String? = null, // High-res for Detail Screen
    val thumbnailUrl: String? = null, // Low-res for List Screen

    // --- QUANTITY (User + API) ---
    val quantity: Double = 1.0, // e.g. 1.0, 0.5 (half left)
    val quantityUnit: String = "", // "pcs", "g", "ml"
    val packagingSize: String? = null, // e.g. "500g" from API

    // --- DATES (The Core Feature) ---
    val expiryDate: LocalDate, // Critical for notifications
    val purchaseDate: LocalDate, // For "How long have I had this?"
    val openDate: LocalDate? = null, // For "Consume within 3 days of opening" logic

    // --- STORAGE ---
    val storageLocation: StorageLocation = StorageLocation.PANTRY,

    // --- METADATA (CRITICAL FOR OFFLINE-FIRST SYNC) ---
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),

    // --- HEALTH & INSIGHTS (From OFF API - The "Perfect" App Features) ---
    // NutriScore: 'a' (healthy) to 'e' (unhealthy). Color code this in UI!
    val nutriScore: String? = null,

    // Nova Group: 1 (Unprocessed) to 4 (Ultra-processed). Warn user if 4.
    val novaGroup: Int? = null,

    // EcoScore: Environmental impact. 'a' to 'e'.
    val ecoScore: String? = null,

    // Allergens: List of tags like "en:peanuts". Compare with user settings to show RED warnings.
    val allergens: List<String> = emptyList(),

    // Labels: e.g. "en:vegan", "en:gluten-free". Use for icons/badges.
    val labels: List<String> = emptyList(),

    // Nutrition Dashboard (Per 100g/ml) - Nullable because not all products have it
    val caloriesPer100g: Int? = null,
    val sugarPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val proteinPer100g: Double? = null
)

enum class StorageLocation {
    PANTRY, FRIDGE, FREEZER
}