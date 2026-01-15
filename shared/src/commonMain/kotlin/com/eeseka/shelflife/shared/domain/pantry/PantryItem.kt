package com.eeseka.shelflife.shared.domain.pantry

import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class PantryItem(
    // --- IDENTITY (Local & API) ---
    val id: String,
    val barcode: String,
    val name: String,
    val brand: String? = null,

    // --- IMAGES ---
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,

    // --- QUANTITY ---
    val quantity: Double = 1.0,
    val quantityUnit: String = "",
    val packagingSize: String? = null,

    // --- DATES (The Core Feature) ---
    val expiryDate: LocalDate,
    val purchaseDate: LocalDate,
    val openDate: LocalDate? = null,

    // --- STORAGE ---
    val storageLocation: StorageLocation = StorageLocation.PANTRY,

    // --- SYNC METADATA ---
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),

    // --- HEALTH & INSIGHTS  ---
    val nutriScore: String? = null,
    val novaGroup: Int? = null,
    val ecoScore: String? = null,
    val allergens: List<String> = emptyList(),
    val labels: List<String> = emptyList(),
    val caloriesPer100g: Int? = null,
    val sugarPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val proteinPer100g: Double? = null
)


enum class StorageLocation {
    PANTRY, FRIDGE, FREEZER
}