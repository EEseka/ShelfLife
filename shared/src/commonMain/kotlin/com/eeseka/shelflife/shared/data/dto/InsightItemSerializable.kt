package com.eeseka.shelflife.shared.data.dto

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Serializable
data class InsightItemSerializable(
    // --- Identity ---
    val id: String,
    val name: String,
    val imageUrl: String?,
    val quantity: Double,
    val quantityUnit: String,

    // --- The Analytics ---
    val status: String, // "CONSUMED" or "WASTED"
    val actionDate: String,  // Date stored as ISO-8601 strings for Firestore compatibility

    // --- SYNC METADATA ---
    val updatedAt: Long,

    // --- Health Analytics (From PantryItem) ---
    val nutriScore: String?,
    val novaGroup: Int?,
    val ecoScore: String?
)
