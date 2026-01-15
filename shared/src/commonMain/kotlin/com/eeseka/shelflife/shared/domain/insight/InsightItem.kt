package com.eeseka.shelflife.shared.domain.insight

import kotlinx.datetime.LocalDate

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

    // --- Health Analytics (From PantryItem) ---
    val nutriScore: String?,   // Did I eat healthy stuff or waste healthy stuff?
    val novaGroup: Int?,       // Did I eat processed food?
    val ecoScore: String?
)

enum class InsightStatus {
    CONSUMED,
    WASTED
}