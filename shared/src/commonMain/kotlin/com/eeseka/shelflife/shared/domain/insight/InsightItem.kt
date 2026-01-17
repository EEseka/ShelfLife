package com.eeseka.shelflife.shared.domain.insight

import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class InsightItem(
    // --- Identity ---
    val id: String,
    val name: String,
    val imageUrl: String?,
    val quantity: Double,
    val quantityUnit: String,

    // --- The Analytics ---
    val status: InsightStatus,
    val actionDate: LocalDate,

    // --- SYNC METADATA ---
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),

    // --- Health Analytics (From PantryItem) ---
    val nutriScore: String?,   // Did I eat healthy stuff or waste healthy stuff?
    val novaGroup: Int?,       // Did I eat processed food?
    val ecoScore: String?
)

enum class InsightStatus {
    CONSUMED,
    WASTED
}